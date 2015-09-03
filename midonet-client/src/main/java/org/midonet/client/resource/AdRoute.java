/*
 * Copyright 2014 Midokura SARL
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

package org.midonet.client.resource;

import java.net.URI;
import java.util.UUID;

import org.midonet.client.WebResource;
import org.midonet.client.dto.DtoAdRoute;
import org.midonet.cluster.services.rest_api.MidonetMediaTypes;


public class AdRoute extends ResourceBase<AdRoute, DtoAdRoute> {

    public AdRoute(WebResource resource, URI uriForCeration,
                   DtoAdRoute adRoute) {
        super(resource, uriForCeration, adRoute,
                MidonetMediaTypes.APPLICATION_AD_ROUTE_JSON());
    }

    @Override
    public URI getUri() {
        return principalDto.getUri();
    }

    public int getPrefixLength() {
        return principalDto.getPrefixLength();
    }

    public String getNwPrefix() {
        return principalDto.getNwPrefix();
    }

    public UUID getId() {
        return principalDto.getId();
    }


    public AdRoute nwPrefix(String nwPrefix) {
        principalDto.setNwPrefix(nwPrefix);
        return this;
    }

    public AdRoute prefixLength(int prefixLength) {
        principalDto.setPrefixLength(prefixLength);
        return this;
    }

}
