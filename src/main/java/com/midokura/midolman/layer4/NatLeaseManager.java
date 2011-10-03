package com.midokura.midolman.layer4;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openflow.protocol.OFMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.midokura.midolman.eventloop.Reactor;
import com.midokura.midolman.openflow.MidoMatch;
import com.midokura.midolman.packets.IPv4;
import com.midokura.midolman.rules.NatTarget;
import com.midokura.midolman.state.RouterZkManager;
import com.midokura.midolman.util.Cache;

public class NatLeaseManager implements NatMapping {

    private static final Logger log = LoggerFactory
            .getLogger(NatLeaseManager.class);

    public static final int REFRESH_SECONDS = 30;
    public static final String FWD_DNAT_PREFIX = "dnatfwd";
    public static final String REV_DNAT_PREFIX = "dnatrev";
    public static final String FWD_SNAT_PREFIX = "snatfwd";
    public static final String REV_SNAT_PREFIX = "snatrev";

    // The following maps IP addresses to ordered lists of free ports.
    // These structures are meant to be shared by all rules/nat targets.
    // So nat targets for different rules can overlap and we'll still avoid
    // collisions. That's why we don't care about the nat target here.
    // Note that we use a NaviableSet instead of a simple list because different
    // nat targets might use different port ranges for the same ip.
    // Also note that we don't care about ip ranges - nat targets with more than
    // one ip in their range get broken up into separate entries here.
    // This map should be cleared if we lose our connection to ZK.
    Map<Integer, NavigableSet<Short>> ipToFreePortsMap;
    private RouterZkManager routerMgr;
    private UUID routerId;
    private String rtrIdStr;
    private Cache cache;
    private Reactor reactor;
    private Map<MidoMatch, Set<String>> matchToNatKeys;
    private Map<MidoMatch, ScheduledFuture> matchToFuture;
    private Random rand;

    public NatLeaseManager(RouterZkManager routerMgr, UUID routerId,
            Cache cache, Reactor reactor) {
        this.routerMgr = routerMgr;
        this.ipToFreePortsMap = new HashMap<Integer, NavigableSet<Short>>();
        this.routerId = routerId;
        rtrIdStr = routerId.toString();
        this.cache = cache;
        this.reactor = reactor;
        this.rand = new Random();
        this.matchToNatKeys = new HashMap<MidoMatch, Set<String>>();
        this.matchToFuture = new HashMap<MidoMatch, ScheduledFuture>();
    }

    private class RefreshNatMappings implements Runnable {
        MidoMatch match;

        private RefreshNatMappings(MidoMatch match) {
            this.match = match;
        }

        @Override
        public void run() {
            Set<String> refreshKeys = matchToNatKeys.get(match);
            if (null == refreshKeys) {
                // The match's flow must have expired, stop refreshing.
                return;
            }
            // Refresh all the nat keys associated with this match.
            for (String key : refreshKeys) {
                cache.getAndTouch(key);
            }
            // Re-schedule this runnable if it's the 
            reactor.schedule(this, REFRESH_SECONDS, TimeUnit.SECONDS);
        }
        
    }

    @Override
    public NwTpPair allocateDnat(int nwSrc, short tpSrc, int oldNwDst,
            short oldTpDst, Set<NatTarget> nats, MidoMatch origMatch) {
        log.debug("allocateDnat: nwSrc {} tpSrc {} oldNwDst {} oldTpDst {} nats {}", new Object[] {
                nwSrc,
                tpSrc,
                oldNwDst,
                oldTpDst,
                nats});
        
        // This throws IllegalArgumentException if nats.size() is zero.
        int natPos = rand.nextInt(nats.size());
        Iterator<NatTarget> iter = nats.iterator();
        NatTarget nat = null;
        for (int i = 0; i <= natPos; i++)
            nat = iter.next();
        int newNwDst = rand.nextInt(nat.nwEnd - nat.nwStart + 1) + nat.nwStart;
        short newTpDst = (short) (rand.nextInt(nat.tpEnd - nat.tpStart + 1) + nat.tpStart);
        log.debug("{} DNAT allocated new DST {}:{} to flow from {}:{} to "
                + "{}:{}", new Object[] { rtrIdStr,
                IPv4.fromIPv4Address(newNwDst), newTpDst,
                IPv4.fromIPv4Address(nwSrc), tpSrc,
                IPv4.fromIPv4Address(oldNwDst), oldTpDst });

        Set<String> refreshKeys = matchToNatKeys.get(origMatch);
        if (null == refreshKeys) {
            refreshKeys = new HashSet<String>();
            matchToNatKeys.put(origMatch, refreshKeys);
            ScheduledFuture future = reactor.schedule(new RefreshNatMappings(
                    origMatch), REFRESH_SECONDS, TimeUnit.SECONDS);
            matchToFuture.put(origMatch, future);
        }
        String key = makeCacheKey(FWD_DNAT_PREFIX, nwSrc, tpSrc, oldNwDst,
                oldTpDst);
        refreshKeys.add(key);
        cache.set(key, makeCacheValue(newNwDst, newTpDst));
        key = makeCacheKey(REV_DNAT_PREFIX, nwSrc, tpSrc, newNwDst, newTpDst);
        refreshKeys.add(key);
        cache.set(key, makeCacheValue(oldNwDst, oldTpDst));
        return new NwTpPair(newNwDst, newTpDst);
    }

    public static String makeCacheKey(String prefix, int nwSrc, short tpSrc,
            int nwDst, short tpDst) {
        return String.format("%s%08x%d%08x%d", prefix, nwSrc, tpSrc, nwDst,
                tpDst);
    }

    public static String makeCacheValue(int nwAddr, short tpPort) {
        return String.format("%08x/%d", nwAddr, tpPort);
    }

    private NwTpPair lookupNwTpPair(String key) {
        log.debug("lookupNwTpPair: {}", key);
        
        String value = cache.get(key);
        if (null == value)
            return null;
        String[] parts = value.split("/");
        return new NwTpPair((int) Long.parseLong(parts[0], 16), (short) Integer
                .parseInt(parts[1]));
    }

    @Override
    public NwTpPair lookupDnatFwd(int nwSrc, short tpSrc, int oldNwDst,
            short oldTpDst) {
        log.debug("lookupDnatFwd: nwSrt {} tpSrc {} oldNwDst {} oldTpDst {}",
                new Object[] {nwSrc, tpSrc, oldNwDst, oldTpDst});

        return lookupNwTpPair(makeCacheKey(FWD_DNAT_PREFIX, nwSrc, tpSrc,
                oldNwDst, oldTpDst));
    }

    @Override
    public NwTpPair lookupDnatRev(int nwSrc, short tpSrc, int newNwDst,
            short newTpDst) {
        log.debug("lookupDnatFwd: nwSrc {} tpSrc {} newNwDst {} newTpDst {}",
                new Object[] {nwSrc, tpSrc, newNwDst, newTpDst});
        
        return lookupNwTpPair(makeCacheKey(REV_DNAT_PREFIX, nwSrc, tpSrc,
                newNwDst, newTpDst));
    }

    private boolean makeSnatReservation(int oldNwSrc, short oldTpSrc,
            int newNwSrc, short newTpSrc, int nwDst, short tpDst,
            MidoMatch match) {
        log.debug("makeSnatReservation: oldNwSrc {} oldTpSrc {} newNwSrc {} newTpSrc {} tpDst",
                new Object[] {oldNwSrc, oldTpSrc, newNwSrc, newTpSrc, tpDst});
        
        String reverseKey = makeCacheKey(REV_SNAT_PREFIX, newNwSrc, newTpSrc, nwDst,
                tpDst);
        if (null != cache.get(reverseKey)) {
            log.warn("{} Snat encountered a collision reserving SRC {}:{}",
                    new Object[] { rtrIdStr, IPv4.fromIPv4Address(newNwSrc),
                            newTpSrc });
            return false;
        }
        // If we got here, we can use this port.
        log.debug("{} SNAT reserved new SRC {}:{} for flow from {}:{} to "
                + "{}:{}", new Object[] { rtrIdStr,
                IPv4.fromIPv4Address(newNwSrc), newTpSrc,
                IPv4.fromIPv4Address(oldNwSrc), oldTpSrc,
                IPv4.fromIPv4Address(nwDst), tpDst });
        Set<String> refreshKeys = matchToNatKeys.get(match);
        if (null == refreshKeys) {
            refreshKeys = new HashSet<String>();
            matchToNatKeys.put(match, refreshKeys);
            ScheduledFuture future = reactor.schedule(new RefreshNatMappings(
                    match), REFRESH_SECONDS, TimeUnit.SECONDS);
            matchToFuture.put(match, future);
        }
        String key = makeCacheKey(FWD_SNAT_PREFIX, oldNwSrc, oldTpSrc, nwDst, tpDst);
        refreshKeys.add(key);
        cache.set(key, makeCacheValue(newNwSrc, newTpSrc));
        refreshKeys.add(reverseKey);
        cache.set(reverseKey, makeCacheValue(oldNwSrc, oldTpSrc));
        return true;
    }

    @Override
    public NwTpPair allocateSnat(int oldNwSrc, short oldTpSrc, int nwDst,
            short tpDst, Set<NatTarget> nats, MidoMatch origMatch) {
        // First try to find a port in a block we've already leased.
        for (NatTarget tg : nats) {
            for (int ip = tg.nwStart; ip <= tg.nwEnd; ip++) {
                NavigableSet<Short> freePorts = ipToFreePortsMap.get(ip);
                if (null != freePorts) {
                    // Look for a port in the desired range
                    Short port = freePorts.ceiling(tg.tpStart);
                    if (null != port && port <= tg.tpEnd) {
                        // We've found a free port.
                        freePorts.remove(port);
                        // Check memcached to make sure the port's really free.
                        if (makeSnatReservation(oldNwSrc, oldTpSrc, ip, port,
                                nwDst, tpDst, origMatch))
                            return new NwTpPair(ip, port);
                    }
                    // Else - no free ports for this ip and port range
                }
            }
            // No free ports for this NatTarget
        }
        // None of our leased blocks were suitable. Try leasing another block.
        // TODO: Do something smarter. See:
        // https://sites.google.com/a/midokura.jp/wiki/midonet/srcnat-block-reservations
        int block_size = 100; // TODO: make this configurable?
        int numExceptions = 0;
        for (NatTarget tg : nats) {
            for (int ip = tg.nwStart; ip <= tg.nwEnd; ip++) {
                NavigableSet<Short> reservedBlocks;
                try {
                    reservedBlocks = routerMgr.getSnatBlocks(routerId, ip);
                } catch (Exception e) {
                    return null;
                }
                // Note that Shorts in this sorted set should only be
                // multiples of 100 because that's how we avoid
                // collisions/re-leasing. A Short s represents a lease on
                // the port range [s, s+99] inclusive.
                // Round down tpStart to the nearest 100.
                short block = (short) ((tg.tpStart / block_size) * block_size);
                Iterator<Short> iter = reservedBlocks.tailSet(block, true)
                        .iterator();
                // Find the first lowPort + 100*x that isn't in the tail-set
                // and is less than tg.tpEnd
                while (iter.hasNext()) {
                    Short lease = iter.next();
                    if (lease > block) {
                        // No one reserved the current value of startBlock.
                        // Let's lease it ourselves.
                        break;
                    }
                    if (lease < block) {
                        // this should never happen. someone leased a
                        // block that doesn't start at a multiple of 100
                        continue;
                    }
                    // The normal case. The block is already leased, try
                    // the next one.
                    block += block_size;
                    if (block > tg.tpEnd)
                        break;
                }
                if (block > tg.tpEnd)
                    // No free blocks for this ip. Try the next ip.
                    break;
                try {
                    routerMgr.addSnatReservation(routerId, ip, block);
                } catch (Exception e) {
                    numExceptions++;
                    if (numExceptions > 1)
                        return null;
                    continue;
                }
                // Expand the port block.
                NavigableSet<Short> freePorts = ipToFreePortsMap.get(ip);
                if (null == freePorts) {
                    freePorts = new TreeSet<Short>();
                    ipToFreePortsMap.put(ip, freePorts);
                }
                for (int i = 0; i < block_size; i++)
                    freePorts.add((short) (block + i));
                // Now, starting with the smaller of 'block' and tg.tpStart
                // see if the mapping really is free in Memcached by making sure
                // that the reverse mapping isn't already taken. Note that the
                // common case for snat requires 4 calls to Memcached (one to
                // check whether we've already seen the forward flow, one to
                // make sure the newIp, newPort haven't already been used with
                // the nwDst and tpDst, and 2 to actually store the forward
                // and reverse mappings).
                short freePort = block;
                if (freePort < tg.tpStart)
                    freePort = tg.tpStart;
                while (true) {
                    freePorts.remove(freePort);
                    if (makeSnatReservation(oldNwSrc, oldTpSrc, ip, freePort,
                            nwDst, tpDst, origMatch))
                        return new NwTpPair(ip, freePort);
                    freePort++;
                    if (0 == freePort % block_size || freePort > tg.tpEnd)
                        return null;
                }
            } // End for loop over ip addresses in a nat target.
        } // End for loop over nat targets.
        return null;
    }

    @Override
    public NwTpPair lookupSnatFwd(int oldNwSrc, short oldTpSrc, int nwDst,
            short tpDst) {
        log.debug("lookupSnatFwd: oldNwSrc {} oldTpSrc {} nwDst {} tpDst",
                new Object[] {oldNwSrc, oldTpSrc, nwDst, tpDst});
        
        return lookupNwTpPair(makeCacheKey(FWD_SNAT_PREFIX, oldNwSrc, oldTpSrc,
                nwDst, tpDst));
    }

    @Override
    public NwTpPair lookupSnatRev(int newNwSrc, short newTpSrc, int nwDst,
            short tpDst) {
        log.debug("lookupSnatRev: newNwSrc {} newTpSrc {} nwDst {} tpDst",
                new Object[] {newNwSrc, newTpSrc, nwDst, tpDst});
        
        return lookupNwTpPair(makeCacheKey(REV_SNAT_PREFIX, newNwSrc, newTpSrc,
                nwDst, tpDst));
    }

    @Override
    public void updateSnatTargets(Set<NatTarget> targets) {
        log.warn("updateSnatTargets: {}", targets);
        
        // TODO Auto-generated method stub

    }

    @Override
    public void freeFlowResources(OFMatch match) {
        log.debug("freeFlowResources: match {}", match);
        
        // Cancel refreshing of any keys associated with this match.
        matchToNatKeys.remove(match);
        ScheduledFuture future = matchToFuture.remove(match);
        if (null != future)
            future.cancel(false);
    }

}
