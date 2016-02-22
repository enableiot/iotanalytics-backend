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

package com.intel.databackend.handlers.requestvalidator;

import com.intel.databackend.datastructures.requests.AdvDataInquiryRequest;
import com.intel.databackend.exceptions.*;
import org.apache.commons.collections.CollectionUtils;


public class AdvanceDataRequestValidator implements RequestValidator {

    private final AdvDataInquiryRequest advDataInquiryRequest;

    public AdvanceDataRequestValidator(AdvDataInquiryRequest advDataInquiryRequest) {

        this.advDataInquiryRequest = advDataInquiryRequest;
    }

    @Override
    public void validate() throws IllegalDataInquiryArgumentException {
        if (!hasStartOrEndTimestamp()) {
            throw new IllegalDataInquiryArgumentException(ErrorMsg.NO_START_OR_END_TIMESTAMP);
        }

        if (hasZeroComponentRowLimit()) {
            throw new IllegalDataInquiryArgumentException(ErrorMsg.ZERO_COMPONENT_ROW_LIMIT);
        }

        if (hasEmptyDeviceDataList()) {
            throw new IllegalDataInquiryArgumentException(ErrorMsg.NO_DEVICE_DATA);
        }
    }

    private boolean hasZeroComponentRowLimit() {
        return (advDataInquiryRequest.getComponentRowLimit() != null && advDataInquiryRequest.getComponentRowLimit() <= 0);
    }

    private boolean hasStartOrEndTimestamp() {
        return (advDataInquiryRequest.getStartTimestamp() != null && advDataInquiryRequest.getEndTimestamp() != null);
    }

    private boolean hasEmptyDeviceDataList()  {
        return (CollectionUtils.isEmpty(advDataInquiryRequest.getDeviceDataList()));
    }
}
