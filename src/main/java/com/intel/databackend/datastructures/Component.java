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

import java.util.List;



@JsonInclude(JsonInclude.Include.NON_NULL)
public class Component {

    private String componentId;
    private List<String> samplesHeader;   // ["Timestamp", "Value", "lat", "lon", "alt", "att1", "att2"]
    private List<List<String>> samples;   // [<timestamp_2>,"<sample_2>",latitude,longitude,altitude],

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public List<String> getSamplesHeader() {
        return samplesHeader;
    }

    public void setSamplesHeader(List<String> samplesHeader) {
        this.samplesHeader = samplesHeader;
    }

    public List<List<String>> getSamples() {
        return samples;
    }

    public void setSamples(List<List<String>> samples) {
        this.samples = samples;
    }

    public String toString() {
    return JsonWriter.objectToJson(this);
  }
}
