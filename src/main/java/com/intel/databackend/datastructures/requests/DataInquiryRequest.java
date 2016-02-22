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
import com.intel.databackend.datastructures.ComponentDataType;

import java.util.List;
import java.util.Map;


public class DataInquiryRequest {

    private String msgType;
    private Boolean countOnly;

    private Long startDate;
    private Long endDate;

    private Long maxPoints;

    private Map<String, ComponentDataType> componentsWithDataType;
    private List<String> componentAttributes;

    private Boolean queryMeasureLocation;
    
    public String toString() {
      return JsonWriter.objectToJson(this);
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Long getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(Long maxPoints) {
        this.maxPoints = maxPoints;
    }

    public List<String> getComponentAttributes() {
        return componentAttributes;
    }

    public void setComponentAttributes(List<String> componentAttributes) {
        this.componentAttributes = componentAttributes;
    }

    public Boolean getQueryMeasureLocation() {
        return queryMeasureLocation;
    }

    public void setQueryMeasureLocation(Boolean queryMeasureLocation) {
        this.queryMeasureLocation = queryMeasureLocation;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public Boolean getCountOnly() {
        return countOnly;
    }

    public void setCountOnly(Boolean countOnly) {
        this.countOnly = countOnly;
    }

    public Map<String, ComponentDataType> getComponentsWithDataType() {
        return componentsWithDataType;
    }

    public void setComponentsWithDataType(Map<String, ComponentDataType> componentsWithDataType) {
        this.componentsWithDataType = componentsWithDataType;
    }
}