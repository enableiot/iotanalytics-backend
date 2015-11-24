/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.databackend.datastructures.requests;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.databackend.datastructures.DeviceSearchCriterium;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceSearchRequest {

    private DeviceSearchCriterium deviceId;
    private DeviceSearchCriterium name;
    private DeviceSearchCriterium gatewayId;
    private DeviceSearchCriterium status;
    private DeviceSearchCriterium tags;

    public DeviceSearchCriterium getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(DeviceSearchCriterium deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceSearchCriterium getName() {
        return name;
    }

    public void setName(DeviceSearchCriterium name) {
        this.name = name;
    }

    public DeviceSearchCriterium getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(DeviceSearchCriterium gatewayId) {
        this.gatewayId = gatewayId;
    }

    public DeviceSearchCriterium getTags() {
        return tags;
    }

    public void setTags(DeviceSearchCriterium tags) {
        this.tags = tags;
    }

    public DeviceSearchCriterium getStatus() {
        return status;
    }

    public void setStatus(DeviceSearchCriterium status) {
        this.status = status;
    }

    public String toString() {
        return JsonWriter.objectToJson(this);
    }
}
