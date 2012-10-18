/*
 * Copyright 2012 Midokura Europe SARL
 */
package com.midokura.midolman

import java.util.UUID
import com.midokura.midolman.layer3.Route.NextHop
import com.midokura.midolman.rules.Condition
import com.midokura.midolman.rules.RuleResult.Action
import com.midokura.midonet.cluster.data.{Bridge => ClusterBridge, Chain,
        Port, Ports, Router => ClusterRouter, Route}
import com.midokura.midonet.cluster.data.host.Host
import com.midokura.midonet.cluster.data.rules.LiteralRule
import com.midokura.midonet.cluster.DataClient
import com.midokura.midonet.cluster.data.ports.{LogicalBridgePort,
        LogicalRouterPort, MaterializedBridgePort, MaterializedRouterPort}
import com.midokura.midonet.cluster.data.zones.GreTunnelZone
//import com.midokura.midonet.cluster.data.ports._
import com.midokura.packets.MAC


trait VirtualConfigurationBuilders {

    protected def clusterDataClient(): DataClient

    def newHost(name: String, id: UUID): Host = {
        val host = new Host().setName(name)
        clusterDataClient().hostsCreate(id, host)
        host.setId(id)
        host
    }

    def newHost(name: String): Host = newHost(name, UUID.randomUUID())

    def newOutboundChainOnPort(name: String, port: Port[_, _],
                               id: UUID): Chain = {
        val chain = new Chain().setName(name).setId(id)
        clusterDataClient().chainsCreate(chain)
        port.setOutboundFilter(id)
        chain
    }

    def newOutboundChainOnPort(name: String, port: Port[_, _]): Chain =
        newOutboundChainOnPort(name, port, UUID.randomUUID)

    def newLiteralRuleOnChain(chain: Chain, pos: Int, condition: Condition,
                              action: Action, id: UUID): LiteralRule = {
        val rule = new LiteralRule(condition, action).setId(id)
                        .setChainId(chain.getId).setPosition(pos)
        clusterDataClient().rulesCreate(rule)
        rule
    }

    def newLiteralRuleOnChain(chain: Chain, pos: Int, condition: Condition,
                              action: Action): LiteralRule =
        newLiteralRuleOnChain(chain, pos, condition, action, UUID.randomUUID)

    def greTunnelZone(name: String): GreTunnelZone = {
        val tunnelZone = new GreTunnelZone().setName("default")
        clusterDataClient().tunnelZonesCreate(tunnelZone)
        tunnelZone
    }

    def newBridge(bridge: ClusterBridge): ClusterBridge = {
        clusterDataClient().bridgesGet(
            clusterDataClient().bridgesCreate(bridge))
    }

    def newBridge(name: String): ClusterBridge =
            newBridge(new ClusterBridge().setName(name))

    def newExteriorBridgePort(bridge: ClusterBridge): MaterializedBridgePort = {
        val port = Ports.materializedBridgePort(bridge)
        val uuid = clusterDataClient().portsCreate(port)
        port.setId(uuid)
    }

    def newInteriorBridgePort(bridge: ClusterBridge): LogicalBridgePort = {
        val port = Ports.logicalBridgePort(bridge)
        val uuid = clusterDataClient().portsCreate(port)
        port.setId(uuid)
    }

    def materializePort(port: Port[_, _], host: Host, name: String) {
        clusterDataClient().hostsAddVrnPortMapping(host.getId, port.getId, name)
    }

    def deletePort(port: Port[_, _], host: Host){
        clusterDataClient().hostsDelVrnPortMapping(host.getId, port.getId)
    }

    def newRouter(router: ClusterRouter): ClusterRouter = {
        clusterDataClient().routersGet(clusterDataClient().routersCreate(router))
    }

    def newRouter(name: String): ClusterRouter =
            newRouter(new ClusterRouter().setName(name))

    def newExteriorRouterPort(router: ClusterRouter, port: MaterializedRouterPort) =
        clusterDataClient().portsGet(clusterDataClient().portsCreate(port))
            .asInstanceOf[MaterializedRouterPort]

    def newExteriorRouterPort(router: ClusterRouter, mac: MAC, portAddr: String,
                        nwAddr: String, nwLen: Int): MaterializedRouterPort = {
        newExteriorRouterPort(router, Ports.materializedRouterPort(router)
            .setPortAddr(portAddr)
            .setNwAddr(nwAddr)
            .setNwLength(nwLen)
            .setHwAddr(mac))
    }

    def newInteriorRouterPort(router: ClusterRouter, port: LogicalRouterPort) =
        clusterDataClient().portsGet(clusterDataClient().portsCreate(port))
            .asInstanceOf[LogicalRouterPort]

    def newInteriorRouterPort(router: ClusterRouter, mac: MAC, portAddr: String,
                              nwAddr: String, nwLen: Int): LogicalRouterPort = {
        newInteriorRouterPort(router, Ports.logicalRouterPort(router)
            .setPortAddr(portAddr)
            .setNwAddr(nwAddr)
            .setNwLength(nwLen)
            .setHwAddr(mac))
    }

    def newRoute(router: ClusterRouter,
                 srcNw: String, srcNwLen: Int, dstNw: String, dstNwLen: Int,
                 nextHop: NextHop, nextHopPort: UUID, nextHopGateway: String,
                 weight: Int): UUID = {
        clusterDataClient().routesCreate(new Route()
            .setRouterId(router.getId)
            .setSrcNetworkAddr(srcNw)
            .setSrcNetworkLength(srcNwLen)
            .setDstNetworkAddr(dstNw)
            .setDstNetworkLength(dstNwLen)
            .setNextHop(nextHop)
            .setNextHopPort(nextHopPort)
            .setNextHopGateway(nextHopGateway)
            .setWeight(weight))
    }
}
