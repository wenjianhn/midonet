/*
 * Copyright 2012 Midokura Europe SARL
 */

package com.midokura.sdn.flows;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.midokura.packets.Ethernet;
import com.midokura.packets.IPv4;
import com.midokura.packets.IntIPv4;
import com.midokura.packets.MAC;
import com.midokura.packets.MalformedPacketException;
import com.midokura.packets.Transport;

public class WildcardMatch implements Cloneable, PacketMatch {

    private EnumSet<Field> usedFields = EnumSet.noneOf(Field.class);

    public enum Field {
        InputPortNumber,
        InputPortUUID,
        TunnelID,
        EthernetSource,
        EthernetDestination,
        EtherType,
        NetworkSource,
        NetworkDestination,
        NetworkProtocol,
        NetworkTTL,
        IsIPv4Fragment,
        TransportSource,
        TransportDestination
    }

    /**
     * @return the set of Fields that have been set in this instance.
     */
    @Nonnull
    public Set<Field> getUsedFields() {
        return usedFields;
    }

    private Short inputPortNumber;
    private UUID inputPortUUID;
    private Long tunnelID;
    private MAC ethernetSource;
    private MAC ethernetDestination;
    private Short etherType;
    private IntIPv4 networkSource;
    private IntIPv4 networkDestination;
    private Byte networkProtocol;
    private Byte networkTTL;
    private Boolean isIPv4Fragment;
    private Short transportSource;
    private Short transportDestination;

    @Nonnull
    public WildcardMatch setInputPortNumber(short inputPortNumber) {
        usedFields.add(Field.InputPortNumber);
        this.inputPortNumber = inputPortNumber;
        return this;
    }

    @Nonnull
    public WildcardMatch setInputPort(short inputPortNumber) {
        return setInputPortNumber(inputPortNumber);
    }

    @Nullable
    public Short getInputPort() {
        return getInputPortNumber();
    }

    @Nonnull
    public WildcardMatch unsetInputPortNumber() {
        usedFields.remove(Field.InputPortNumber);
        this.inputPortNumber = null;
        return this;
    }

    @Nullable
    public Short getInputPortNumber() {
        return inputPortNumber;
    }

    @Nonnull
    public WildcardMatch setInputPortUUID(@Nonnull UUID inputPortID) {
        usedFields.add(Field.InputPortUUID);
        this.inputPortUUID = inputPortID;
        return this;
    }

    @Nonnull
    public WildcardMatch unsetInputPortUUID() {
        usedFields.remove(Field.InputPortUUID);
        this.inputPortUUID = null;
        return this;
    }

    @Nullable
    public UUID getInputPortUUID() {
        return inputPortUUID;
    }

    @Nonnull
    public WildcardMatch setTunnelID(long tunnelID) {
        this.tunnelID = tunnelID;
        usedFields.add(Field.TunnelID);
        return this;
    }

    @Nonnull
    public WildcardMatch unsetTunnelID() {
        usedFields.remove(Field.TunnelID);
        this.tunnelID = null;
        return this;
    }

    @Nullable
    public Long getTunnelID() {
        return tunnelID;
    }


    @Nonnull
    public WildcardMatch setEthernetSource(@Nonnull MAC addr) {
        usedFields.add(Field.EthernetSource);
        this.ethernetSource = addr;
        return this;
    }

    public WildcardMatch setDataLayerSource(String macaddr) {
        return setDataLayerSource(MAC.fromString(macaddr));
    }

    @Override
    public WildcardMatch setDataLayerSource(MAC addr) {
        if (addr != null)
            setEthernetSource(addr);
        return this;
    }

    @Nonnull
    public WildcardMatch unsetEthernetSource() {
        usedFields.remove(Field.EthernetSource);
        this.ethernetSource = null;
        return this;
    }

    @Nullable
    public MAC getEthernetSource() {
        return ethernetSource;
    }

    @Override
    public byte[] getDataLayerSource() {
        return ethernetSource.getAddress();
    }

    @Nonnull
    public WildcardMatch setEthernetDestination(@Nonnull MAC addr) {
        usedFields.add(Field.EthernetDestination);
        this.ethernetDestination = addr;
        return this;
    }

    public WildcardMatch setDataLayerDestination(String macAddr) {
        return setDataLayerDestination(MAC.fromString(macAddr));
    }

    @Override
    public WildcardMatch setDataLayerDestination(MAC addr) {
        if (addr != null)
            setEthernetDestination(addr);
        return this;
    }

    @Nonnull
    public WildcardMatch unsetEthernetDestination() {
        usedFields.remove(Field.EthernetDestination);
        this.ethernetDestination = null;
        return this;
    }

    @Nullable
    public MAC getEthernetDestination() {
        return ethernetDestination;
    }

    @Override
    public byte[] getDataLayerDestination() {
        return ethernetDestination.getAddress();
    }

    @Nonnull
    public WildcardMatch setEtherType(short etherType) {
        usedFields.add(Field.EtherType);
        this.etherType = etherType;
        return this;
    }

    @Nonnull
    public WildcardMatch unsetEtherType() {
        usedFields.remove(Field.EtherType);
        this.etherType = null;
        return this;
    }

    @Nullable
    public Short getEtherType() {
        return etherType;
    }

    @Nonnull
    public WildcardMatch setDataLayerType(short dlType) {
        return setEtherType(dlType);
    }

    @Override
    public short getDataLayerType() {
        return etherType.shortValue();
    }

    @Nonnull
    public WildcardMatch setNetworkSource(@Nonnull IntIPv4 addr) {
        usedFields.add(Field.NetworkSource);
        this.networkSource = addr;
        return this;
    }

    @Override
    public WildcardMatch setNetworkSource(int addr) {
        return setNetworkSource(new IntIPv4(addr));
    }

    public WildcardMatch setNetworkSource(int addr, int maskLen) {
        return setNetworkSource(new IntIPv4(addr, maskLen));
    }

    @Nonnull
    public WildcardMatch unsetNetworkSource() {
        usedFields.remove(Field.NetworkSource);
        this.networkSource = null;
        return this;
    }

    @Nullable
    public IntIPv4 getNetworkSourceIPv4() {
        return networkSource;
    }

    @Override
    public int getNetworkSource() {
        return networkSource.addressAsInt();
    }

    @Nonnull
    public WildcardMatch setNetworkDestination(@Nonnull IntIPv4 addr) {
        usedFields.add(Field.NetworkDestination);
        this.networkDestination = addr;
        return this;
    }

    @Override
    public WildcardMatch setNetworkDestination(int addr) {
        return setNetworkDestination(new IntIPv4(addr));
    }

    public WildcardMatch setNetworkDestination(int addr, int maskLen) {
        return setNetworkDestination(new IntIPv4(addr, maskLen));
    }

    @Nonnull
    public WildcardMatch unsetNetworkDestination() {
        usedFields.remove(Field.NetworkDestination);
        this.networkDestination = null;
        return this;
    }

    @Nullable
    public IntIPv4 getNetworkDestinationIPv4() {
        return networkDestination;
    }

    @Override
    public int getNetworkDestination() {
        return networkDestination.addressAsInt();
    }

    @Nonnull
    public WildcardMatch setNetworkProtocol(byte networkProtocol) {
        usedFields.add(Field.NetworkProtocol);
        this.networkProtocol = networkProtocol;
        return this;
    }

    @Nonnull
    public WildcardMatch unsetNetworkProtocol() {
        usedFields.remove(Field.NetworkProtocol);
        this.networkProtocol = null;
        return this;
    }

    @Nullable
    public Byte getNetworkProtocolObject() {
        return networkProtocol;
    }

    @Override
    public byte getNetworkProtocol() {
        return networkProtocol.byteValue();
    }

    @Override
    public byte getNetworkTypeOfService() {
        // TODO(pino): WCMatch has no NetworkTOS, but rules.Condition
        // checks the TOS of the match.  Reconcile.
        return 0;
    }

    public WildcardMatch setNetworkTypeOfService(byte tos) {
        // TODO(pino): implement this.
        return this;
    }

    @Nonnull
    public WildcardMatch setNetworkTTL(byte networkTTL) {
        usedFields.add(Field.NetworkTTL);
        this.networkTTL = networkTTL;
        return this;
    }

    @Nonnull
    public WildcardMatch unsetNetworkTTL() {
        usedFields.remove(Field.NetworkTTL);
        this.networkTTL = null;
        return this;
    }

    @Nullable
    public Byte getNetworkTTL() {
        return networkTTL;
    }

    @Nonnull
    public WildcardMatch setIsIPv4Fragment(boolean isFragment) {
        usedFields.add(Field.IsIPv4Fragment);
        this.isIPv4Fragment = isFragment;
        return this;
    }

    @Nonnull
    public WildcardMatch unsetIsIPv4Fragment() {
        usedFields.remove(Field.IsIPv4Fragment);
        this.isIPv4Fragment = null;
        return this;
    }

    @Nullable
    public Boolean getIsIPv4Fragment() {
        return isIPv4Fragment;
    }

    @Nonnull @Override
    public WildcardMatch setTransportSource(short transportSource) {
        usedFields.add(Field.TransportSource);
        this.transportSource = transportSource;
        return this;
    }

    @Nonnull
    public WildcardMatch unsetTransportSource() {
        usedFields.remove(Field.TransportSource);
        this.transportSource = null;
        return this;
    }

    @Nullable
    public Short getTransportSourceObject() {
        return transportSource;
    }

    @Override
    public short getTransportSource() {
        return transportSource.shortValue();
    }

    @Nonnull @Override
    public WildcardMatch setTransportDestination(short transportDestination) {
        usedFields.add(Field.TransportDestination);
        this.transportDestination = transportDestination;
        return this;
    }

    @Nonnull
    public WildcardMatch unsetTransportDestination() {
        usedFields.remove(Field.TransportDestination);
        this.transportDestination = null;
        return this;
    }

    @Nullable
    public Short getTransportDestinationObject() {
        return transportDestination;
    }

    @Override
    public short getTransportDestination() {
        return transportDestination.shortValue();
    }

    /**
     * Applies this match to a fresh copy of an ethernet packet
     *
     * @param toPacket Packet to apply match to (read-only)
     * @return A newly allocated packet with the fields in the match applied
     * @throws MalformedPacketException
     */
    public Ethernet apply(Ethernet toPacket) throws MalformedPacketException {
        if (toPacket == null)
            return null;

        Ethernet eth = Ethernet.deserialize(toPacket.serialize());
        IPv4 ipv4 = null;
        Transport transport = null;

        if (eth.getPayload() instanceof IPv4)
            ipv4 = (IPv4) eth.getPayload();
        if (ipv4 != null && ipv4.getPayload() instanceof Transport)
            transport = (Transport) ipv4.getPayload();

        /* TODO (guillermo)
         * support (ethernet(ip(tcp|udp))) in the 1st go. A full/correct
         * implementation of apply() should cover all matchable protocols
         * for each network layer
         */
        if (ipv4 == null || transport == null)
             return null;

        for (Field field : getUsedFields()) {
            switch (field) {
                case EtherType:
                    eth.setEtherType(etherType);
                    break;
                case EthernetDestination:
                    eth.setDestinationMACAddress(ethernetDestination);
                    break;
                case EthernetSource:
                    eth.setSourceMACAddress(ethernetSource);
                    break;

                case TransportDestination:
                    transport.setDestinationPort(transportDestination);
                    break;
                case TransportSource:
                    transport.setSourcePort(transportSource);
                    break;

                case NetworkDestination:
                    ipv4.setDestinationAddress(networkDestination.addressAsInt());
                    break;
                case NetworkSource:
                    ipv4.setSourceAddress(networkSource.addressAsInt());
                    break;
                case NetworkProtocol:
                    ipv4.setProtocol(networkProtocol);
                    break;

                case NetworkTTL:
                    ipv4.setTtl(networkTTL);
                    break;

                case IsIPv4Fragment:
                    // XXX guillermo (does it make sense to make changes to
                    // this? it would be useless without changing the offset
                    // accordingly.
                    break;

                case InputPortUUID:
                    break;
                case InputPortNumber:
                    break;
                case TunnelID:
                    break;
            }
        }

        return eth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof WildcardMatch)) return false;

        WildcardMatch that = (WildcardMatch) o;

        for (Field field : getUsedFields()) {
            switch (field) {
                case EtherType:
                    if (!isEqual(etherType, that.etherType))
                        return false;
                    break;

                case IsIPv4Fragment:
                    if (!isEqual(isIPv4Fragment, that.isIPv4Fragment))
                        return false;
                    break;

                case EthernetDestination:
                    if (!isEqual(ethernetDestination, that.ethernetDestination))
                        return false;
                    break;

                case EthernetSource:
                    if (!isEqual(ethernetSource, that.ethernetSource))
                        return false;
                    break;

                case TransportDestination:
                    if (!isEqual(transportDestination, that.transportDestination))
                        return false;
                    break;

                case TransportSource:
                    if (!isEqual(transportSource, that.transportSource))
                        return false;
                    break;

                case InputPortUUID:
                    if (!isEqual(inputPortUUID, that.inputPortUUID))
                        return false;
                    break;

                case InputPortNumber:
                    if (!isEqual(inputPortNumber, that.inputPortNumber))
                        return false;
                    break;

                case NetworkDestination:
                    if (!isEqual(networkDestination, that.networkDestination))
                        return false;
                    break;

                case NetworkSource:
                    if (!isEqual(networkSource, that.networkSource))
                        return false;
                    break;

                case NetworkProtocol:
                    if (!isEqual(networkProtocol, that.networkProtocol))
                        return false;
                    break;

                case NetworkTTL:
                    if (!isEqual(networkTTL, that.networkTTL))
                        return false;
                    break;

                case TunnelID:
                    if (!isEqual(tunnelID, that.tunnelID))
                        return false;

                    break;
            }
        }

        return true;
    }

    private <T> boolean isEqual(T a, T b) {
        return a != null ? a.equals(b) : b == null;
    }

    @Override
    public int hashCode() {
        int result = getUsedFields().hashCode();
        for (Field field : getUsedFields()) {
            switch (field) {
                case EtherType:
                    result = 31 * result + etherType.hashCode();
                    break;
                case IsIPv4Fragment:
                    result = 31 * result + isIPv4Fragment.hashCode();
                    break;
                case EthernetDestination:
                    result = 31 * result + ethernetDestination.hashCode();
                    break;
                case EthernetSource:
                    result = 31 * result + ethernetSource.hashCode();
                    break;
                case TransportDestination:
                    result = 31 * result + transportDestination.hashCode();
                    break;
                case TransportSource:
                    result = 31 * result + transportSource.hashCode();
                    break;
                case InputPortUUID:
                    result = 31 * result + inputPortUUID.hashCode();
                    break;
                case InputPortNumber:
                    result = 31 * result + inputPortNumber.hashCode();
                    break;
                case NetworkDestination:
                    result = 31 * result + networkDestination.hashCode();
                    break;
                case NetworkSource:
                    result = 31 * result + networkSource.hashCode();
                    break;
                case NetworkProtocol:
                    result = 31 * result + networkProtocol.hashCode();
                    break;
                case NetworkTTL:
                    result = 31 * result + networkTTL;
                    break;
                case TunnelID:
                    result = 31 * result + tunnelID.hashCode();
                    break;
            }
        }

        return result;
    }

    /**
     * Implement cloneable interface
     */
    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public WildcardMatch clone() {
        // XXX TODO(pino): validate implementation of clone !
        WildcardMatch newClone = new WildcardMatch();

        newClone.usedFields.addAll(usedFields);
        for (Field field : Field.values()) {
            if (usedFields.contains(field)) {
                switch (field) {
                    case EtherType:
                        newClone.etherType = etherType;
                        break;

                    case IsIPv4Fragment:
                        newClone.isIPv4Fragment = isIPv4Fragment;
                        break;

                    case EthernetDestination:
                        newClone.ethernetDestination =
                            ethernetDestination.clone();
                        break;

                    case EthernetSource:
                        newClone.ethernetSource = ethernetSource.clone();
                        break;

                    case TransportDestination:
                        newClone.transportDestination = transportDestination;
                        break;

                    case TransportSource:
                        newClone.transportSource = transportSource;
                        break;

                    case InputPortUUID:
                        newClone.inputPortUUID = inputPortUUID;
                        break;

                    case InputPortNumber:
                        newClone.inputPortNumber = inputPortNumber;
                        break;

                    case NetworkDestination:
                        newClone.networkDestination = networkDestination.clone();
                        break;

                    case NetworkSource:
                        newClone.networkSource = networkSource.clone();
                        break;

                    case NetworkProtocol:
                        newClone.networkProtocol = networkProtocol;
                        break;

                    case NetworkTTL:
                        newClone.networkTTL = networkTTL;
                        break;

                    case TunnelID:
                        newClone.tunnelID = tunnelID;
                        break;

                    default:
                        throw new RuntimeException("Cannot clone a " +
                            "WildcardMatch with an unrecognized field: " +
                            field);
                }
            }
        }

        return newClone;
    }
}
