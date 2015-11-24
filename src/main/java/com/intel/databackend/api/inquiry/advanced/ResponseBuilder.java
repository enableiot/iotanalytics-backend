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

package com.intel.databackend.api.inquiry.advanced;

import com.intel.databackend.datastructures.DeviceData;
import com.intel.databackend.datastructures.requests.AdvDataInquiryRequest;
import com.intel.databackend.datastructures.responses.AdvDataInquiryResponse;

import java.util.List;

public class ResponseBuilder {

    private final AdvDataInquiryResponse dataInquiryResponse;

    private final AdvDataInquiryRequest dataInquiryRequest;

    public ResponseBuilder(AdvDataInquiryRequest dataInquiryRequest, String accountId) {
        this.dataInquiryRequest = dataInquiryRequest;
        dataInquiryResponse = new AdvDataInquiryResponse();
        dataInquiryResponse.setAccountId(accountId);
    }

    public AdvDataInquiryResponse build(Long rowCount, List<DeviceData> deviceDataList) {
        dataInquiryResponse.setRowCount(rowCount);
        dataInquiryResponse.setData(deviceDataList);
        if (!dataInquiryRequest.isCountOnly()) {
            setTimestampsInResponse();
            setComponentRowLimitsInResponse();
        }
        return  dataInquiryResponse;
    }

    public AdvDataInquiryResponse getDataInquiryResponse() {
        return dataInquiryResponse;
    }

    private void setTimestampsInResponse() {
        dataInquiryResponse.setStartTimestamp(dataInquiryRequest.getStartTimestamp());
        dataInquiryResponse.setEndTimestamp(dataInquiryRequest.getEndTimestamp());
    }

    private void setComponentRowLimitsInResponse() {
        dataInquiryResponse.setComponentRowLimit(dataInquiryRequest.getComponentRowLimit());
        dataInquiryResponse.setComponentRowStart(dataInquiryRequest.getComponentRowStart());
        if (dataInquiryRequest.getComponentRowLimit() != null && dataInquiryRequest.getComponentRowStart() == null) {
            dataInquiryResponse.setComponentRowStart(0L);
        }
    }


}
