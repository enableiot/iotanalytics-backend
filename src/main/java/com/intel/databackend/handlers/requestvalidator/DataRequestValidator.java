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

import com.intel.databackend.datastructures.requests.DataInquiryRequest;
import com.intel.databackend.exceptions.ErrorMsg;
import com.intel.databackend.exceptions.IllegalDataInquiryArgumentException;


public class DataRequestValidator implements RequestValidator {

    private final DataInquiryRequest dataInquiryRequest;

    public DataRequestValidator(DataInquiryRequest dataInquiryRequest) {

        this.dataInquiryRequest = dataInquiryRequest;
    }

    public void validate() throws IllegalDataInquiryArgumentException {
        String error = verifyRequestParams();

        if (error != null) {
            throw new IllegalDataInquiryArgumentException(error);
        }
    }

    private String verifyRequestParams() {
        if (hasNotStartOrEndDate()) {
            return ErrorMsg.NO_START_OR_END_DATE;
        }

        if (hasEmptyComponentsWithDataType()) {
            return ErrorMsg.NO_COMPONENTS_PROVIDED;
        }

        if (hasCountOnlyWithMaxPoints()) {
            return ErrorMsg.COUNT_ONLY_WITH_MAX_POINTS_SELECTED;
        }

        if (isMaxPointsRequest() && hasQueryLocation()) {
            return ErrorMsg.QUERY_LOCATION_WITH_MAX_POINTS_SELECTED;
        }
        return null;
    }

    private boolean hasCountOnlyWithMaxPoints() {
        return (dataInquiryRequest.getCountOnly() != null && dataInquiryRequest.getCountOnly() && dataInquiryRequest.getMaxPoints() != null);
    }

    private boolean hasEmptyComponentsWithDataType()  {
        return (dataInquiryRequest.getComponentsWithDataType() == null);
    }

    private boolean hasNotStartOrEndDate() {
        return (dataInquiryRequest.getStartDate() == null || dataInquiryRequest.getEndDate() == null);
    }

    private boolean hasQueryLocation() {
        return (dataInquiryRequest.getQueryMeasureLocation() != null && dataInquiryRequest.getQueryMeasureLocation() == true);
    }

    private boolean isMaxPointsRequest() {
        return (dataInquiryRequest.getMaxPoints() != null && dataInquiryRequest.getMaxPoints() > 0);
    }


}
