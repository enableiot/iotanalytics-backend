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

package com.intel.databackend.api.inquiry;

import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.requests.AdvDataInquiryRequest;
import com.intel.databackend.datastructures.requests.DataInquiryRequest;

import java.util.List;
import java.util.Map;

public class DataRetrieveParams {

    private final Long startDate;
    private final Long endDate;
    private final Long maxPoints;
    private final String accountId;
    private List<String> componentsAttributes;
    private final Boolean queryMeasureLocation;

    private Map<String, ComponentDataType> componentsMetadata;

    public DataRetrieveParams(DataInquiryRequest dataInquiryRequest, String accountId) {
        this.startDate = dataInquiryRequest.getStartDate();
        this.endDate = dataInquiryRequest.getEndDate();
        this.accountId = accountId;
        this.queryMeasureLocation = dataInquiryRequest.getQueryMeasureLocation();
        this.componentsAttributes = dataInquiryRequest.getComponentAttributes();
        this.maxPoints = dataInquiryRequest.getMaxPoints();
        this.componentsMetadata = dataInquiryRequest.getComponentsWithDataType();
    }

    public DataRetrieveParams(AdvDataInquiryRequest dataInquiryRequest, String accountId) {
        this.startDate = dataInquiryRequest.getStartTimestamp();
        this.endDate = dataInquiryRequest.getEndTimestamp();
        this.accountId = accountId;
        this.queryMeasureLocation = dataInquiryRequest.getShowMeasureLocation();
        this.maxPoints = null;
    }

    public Long getStartDate() {
        return startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public String getAccountId() {
        return accountId;
    }

    public String[] getComponentsAttributes() {
        String[] attributes = null;
        if (componentsAttributes != null) {
            attributes = componentsAttributes.toArray(new String[componentsAttributes.size()]);
        }
        return attributes;
    }

    public boolean isQueryMeasureLocation() {
        if (queryMeasureLocation == null) {
            return false;
        }
        return queryMeasureLocation;
    }

    public Long getMaxPoints() {
        return maxPoints;
    }

    public void setComponentsAttributes(List<String> componentsAttributes) {
        this.componentsAttributes = componentsAttributes;
    }

    public Map<String, ComponentDataType> getComponentsMetadata() {
        return componentsMetadata;
    }

    public void setComponentsMetadata(Map<String, ComponentDataType> componentsMetadata) {
        this.componentsMetadata = componentsMetadata;
    }
}
