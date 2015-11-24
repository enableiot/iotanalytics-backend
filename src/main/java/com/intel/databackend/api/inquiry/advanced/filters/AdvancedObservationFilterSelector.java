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

package com.intel.databackend.api.inquiry.advanced.filters;

import com.intel.databackend.api.helpers.ObservationFilters;
import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.datastructures.requests.AdvDataInquiryRequest;


public class AdvancedObservationFilterSelector implements ObservationFilterSelector {

    private final ObservationFilters observationFilters;
    private final AdvDataInquiryRequest dataInquiryRequest;

    public AdvancedObservationFilterSelector(AdvDataInquiryRequest dataInquiryRequest) {
        observationFilters = new ObservationFilters();
        this.dataInquiryRequest = dataInquiryRequest;
    }

    @Override
    public Observation[] filter(Observation[] obs, ComponentDataType componentDataType) {
        if (dataInquiryRequest.getValueFilter() != null && dataInquiryRequest.getValueFilter().containsKey("value")) {
            obs = observationFilters.filterByValue(obs, dataInquiryRequest.getValueFilter(), componentDataType);
        }
        if (dataInquiryRequest.getMeasurementAttributeFilter() != null) {
            obs = observationFilters.filterByMeasurementAttrs(obs, dataInquiryRequest.getMeasurementAttributeFilter());
        }
        return obs;
    }
}
