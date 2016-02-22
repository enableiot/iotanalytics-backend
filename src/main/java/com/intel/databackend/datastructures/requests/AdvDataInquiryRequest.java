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
import com.intel.databackend.datastructures.DeviceData;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvDataInquiryRequest {
    private String msgType;
    private Boolean countOnly;

    private Long startTimestamp;
    private Long endTimestamp;

    List<DeviceData> deviceDataList;

    private List<String> returnedMeasureAttributes;
    private Boolean showMeasureLocation;
    private String aggregations;
    private Map<String, List<String>> devCompAttributeFilter;
    private Map<String, List<String>> measurementAttributeFilter;
    private Map<String, List<String>> valueFilter;
    private Long componentRowStart;
    private Long componentRowLimit;

    public List<Map<String, String>> sort;

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public List<String> getReturnedMeasureAttributes() {
        return returnedMeasureAttributes;
    }

    public void setReturnedMeasureAttributes(List<String> returnedMeasureAttributes) {
        this.returnedMeasureAttributes = returnedMeasureAttributes;
    }

    public Boolean getShowMeasureLocation() {
        return showMeasureLocation;
    }

    public void setShowMeasureLocation(Boolean showMeasureLocation) {
        this.showMeasureLocation = showMeasureLocation;
    }

    public String getAggregations() {
        return aggregations;
    }

    public void setAggregations(String aggregations) {
        this.aggregations = aggregations;
    }

    public Map<String, List<String>> getDevCompAttributeFilter() {
        return devCompAttributeFilter;
    }

    public void setDevCompAttributeFilter(Map<String, List<String>> devCompAttributeFilter) {
        this.devCompAttributeFilter = devCompAttributeFilter;
    }

    public Map<String, List<String>> getMeasurementAttributeFilter() {
        return measurementAttributeFilter;
    }

    public void setMeasurementAttributeFilter(Map<String, List<String>> measurementAttributeFilter) {
        this.measurementAttributeFilter = measurementAttributeFilter;
    }

    public Map<String, List<String>> getValueFilter() {
        return valueFilter;
    }

    public void setValueFilter(Map<String, List<String>> valueFilter) {
        this.valueFilter = valueFilter;
    }

    public Long getComponentRowStart() {
        return componentRowStart;
    }

    public void setComponentRowStart(Long componentRowStart) {
        this.componentRowStart = componentRowStart;
    }

    public Long getComponentRowLimit() {
        return componentRowLimit;
    }

    public void setComponentRowLimit(Long componentRowLimit) {
        this.componentRowLimit = componentRowLimit;
    }

    public List<Map<String, String>> getSort() {
        return sort;
    }

    public void setSort(List<Map<String, String>> sort) {
        this.sort = sort;
    }

    public String toString() {
    	return JsonWriter.objectToJson(this);
    }

    public String[] getRequestedAttributes() {
        Set<String> attributesSet = new HashSet<>();
        if (returnedMeasureAttributes != null) {
            attributesSet.addAll(returnedMeasureAttributes);
        }
        if (measurementAttributeFilter != null) {
            attributesSet.addAll(measurementAttributeFilter.keySet());
        }
        return attributesSet.toArray(new String[attributesSet.size()]);
    }

    public boolean isCountOnly() {
        return countOnly != null && countOnly == true;
    }

    public boolean hasRequestMeasuredAttributes() {
        return getReturnedMeasureAttributes() != null && getReturnedMeasureAttributes().size() > 0;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setCountOnly(Boolean countOnly) {
        this.countOnly = countOnly;
    }

    public List<DeviceData> getDeviceDataList() {
        return deviceDataList;
    }

    public void setDeviceDataList(List<DeviceData> deviceDataList) {
        this.deviceDataList = deviceDataList;
    }
}
