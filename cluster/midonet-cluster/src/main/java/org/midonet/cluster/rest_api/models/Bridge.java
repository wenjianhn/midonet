/*
 * Copyright 2015 Midokura SARL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.midonet.cluster.rest_api.models;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.midonet.cluster.data.ZoomClass;
import org.midonet.cluster.data.ZoomField;
import org.midonet.cluster.models.Topology;
import org.midonet.cluster.rest_api.annotation.Resource;
import org.midonet.cluster.rest_api.annotation.ResourceId;
import org.midonet.cluster.rest_api.annotation.Subresource;
import org.midonet.cluster.util.UUIDUtil.Converter;
import org.midonet.util.version.Since;

@XmlRootElement(name = "bridge")
@Resource(name = ResourceUris.BRIDGES)
@ZoomClass(clazz = Topology.Network.class)
public class Bridge extends UriResource {

    @ResourceId
    @ZoomField(name = "id", converter = Converter.class)
    public UUID id;

    @ZoomField(name = "tenant_id")
    @NotNull
    public String tenantId;

    @ZoomField(name = "name")
    @NotNull
    public String name;

    @ZoomField(name = "admin_state_up")
    public boolean adminStateUp;

    @ZoomField(name = "inbound_filter_id", converter = Converter.class)
    public UUID inboundFilterId;
    @ZoomField(name = "outbound_filter_id", converter = Converter.class)
    public UUID outboundFilterId;

    // TODO: validation, this field must not be updated by the user
    @Since("2")
    public UUID vxLanPortId;

    // TODO: validation, this field must not be updated by the user
    @ZoomField(name = "vxlan_port_ids", converter = Converter.class)
    @Since("3") // after adding support to multiple vtep bindings
    public List<UUID> vxLanPortIds;

    @XmlTransient
    @Subresource(name = ResourceUris.PORTS)
    @ZoomField(name = "port_ids", converter = Converter.class)
    public List<UUID> portIds;

    @XmlTransient
    @Subresource(name = ResourceUris.DHCP)
    @ZoomField(name = "dhcp_ids", converter = Converter.class)
    public List<UUID> dhcpIds;

    public Bridge() {
        adminStateUp = true;
    }

    public String getVlanMacTableTemplate() {
        return getUri() + "/vlans/{vlanId}/mac_table";
    }

    public String getMacPortTemplate() {
        return getUri() + "/mac_table/{macAddress}_{portId}";
    }

    public String getVlanMacPortTemplate() {
        return getUri() + "/vlans/{vlanId}/mac_table/{macAddress}_{portId}";
    }

    public URI getPorts() {
        return relativeUri(ResourceUris.PORTS);
    }

    public URI getPeerPorts() {
        return relativeUri(ResourceUris.PEER_PORTS);
    }

    @Since("3")
    public List<URI> getVxLanPorts() {
        return absoluteUris(ResourceUris.VXLAN_PORTS, vxLanPortIds);
    }

    @Since("2")
    public URI getVxLanPort() {
        return absoluteUri(ResourceUris.PORTS, vxLanPortId);
    }

    public URI getMacTable() {
        return relativeUri(ResourceUris.MAC_TABLE);
    }

    public URI getArpTable() {
        return relativeUri(ResourceUris.ARP_TABLE);
    }

    public URI getDhcpSubnets() {
        return relativeUri(ResourceUris.DHCP);
    }

    public URI getDhcpSubnet6s() {
        return relativeUri(ResourceUris.DHCPV6);
    }

}
