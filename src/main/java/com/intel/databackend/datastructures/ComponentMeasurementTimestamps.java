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

package com.intel.databackend.datastructures;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComponentMeasurementTimestamps {

    private String componentId;
    private Long firstPointTS;
    private Long lastPointTS;

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public Long getFirstPointTS() {
        return firstPointTS;
    }

    public void setFirstPointTS(Long firstPointTS) {
        this.firstPointTS = firstPointTS;
    }

    public Long getLastPointTS() {
        return lastPointTS;
    }

    public void setLastPointTS(Long lastPointTS) {
        this.lastPointTS = lastPointTS;
    }

    public String toString() {
        return JsonWriter.objectToJson(this);
    }
}
