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

package com.intel.databackend.api.inquiry.basic;

import com.intel.databackend.datastructures.Component;
import com.intel.databackend.datastructures.responses.DataInquiryResponse;

import java.util.List;

public class ResponseBuilder {

    private DataInquiryResponse dataInquiryResponse;

    public ResponseBuilder(String accountId, Long rowCount, List<Component> components) {
        dataInquiryResponse = new DataInquiryResponse();
        dataInquiryResponse.setAccountId(accountId);
        dataInquiryResponse.setRowCount(rowCount);
        dataInquiryResponse.setComponents(components);
    }

    public DataInquiryResponse getDataInquiryResponse() {
        return dataInquiryResponse;
    }
}
