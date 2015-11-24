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

package com.intel.databackend.datastructures.responses;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.intel.databackend.datastructures.DeviceData;

import java.util.ArrayList;
import java.util.List;



@JsonInclude(Include.NON_NULL)
public class AdvDataInquiryResponse {

    private String msgType = "advancedDataInquiryResponse";
    private String accountId;
    private Long startTimestamp;
    private Long endTimestamp;
    private Long componentRowLimit;
    private Long componentRowStart;
    private Boolean hasNext;
    private Long rowCount;
    private List<DeviceData> data;

    public String getMsgType() {
        return msgType;
    }

    public AdvDataInquiryResponse() {
        data = new ArrayList<DeviceData>();
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

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

    public Long getComponentRowLimit() {
        return componentRowLimit;
    }

    public void setComponentRowLimit(Long componentRowLimit) {
        this.componentRowLimit = componentRowLimit;
    }

    public Long getComponentRowStart() {
        return componentRowStart;
    }

    public void setComponentRowStart(Long componentRowStart) {
        this.componentRowStart = componentRowStart;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }

    public List<DeviceData> getData() {
        return data;
    }

    public void setData(List<DeviceData> data) {
        this.data = data;
    }

    public String toString() {
        return JsonWriter.objectToJson(this);
    }
}

