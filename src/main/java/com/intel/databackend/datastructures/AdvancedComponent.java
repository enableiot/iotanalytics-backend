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
public class AdvancedComponent {

    /*
     * these 4 fields we receive from dashboard
     */
    private String componentId;
    private String componentType;
    private String componentName;
    private String dataType;

    /*
     below fields(values) are calculated - we not receive them from dashboard
     */
    private Double max;
    private Double min;
    private Long count;
    private Double sum;
    private Double sumOfSquares;
    private List<String> samplesHeader;
    private List<List<String>> samples;  // [<timestamp_2>,"<sample_2>",latitude,longitude,altitude],

    public String toString() {
        return JsonWriter.objectToJson(this);
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public Double getSumOfSquares() {
        return sumOfSquares;
    }

    public void setSumOfSquares(Double sumOfSquares) {
        this.sumOfSquares = sumOfSquares;
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

    public boolean isNumericType() {
        return getDataType().equals("Number");
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
