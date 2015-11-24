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

package com.intel.databackend.api.inquiry.advanced.componentsbuilder;

import com.intel.databackend.datastructures.requests.AdvDataInquiryRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class ComponentsBuilderParams {

    private final List<Map<String, String>> sort;

    private final List<String> returnedMeasureAttributes;

    private final String aggregations;

    private final Long componentRowLimit;

    private final Long componentRowStart;

    public ComponentsBuilderParams(AdvDataInquiryRequest request, String[] measureAttributes) {
        this.sort = request.getSort();
        this.returnedMeasureAttributes = Arrays.asList(measureAttributes);
        this.aggregations = request.getAggregations();
        this.componentRowLimit = request.getComponentRowLimit();
        this.componentRowStart = request.getComponentRowStart();
    }

    public List<Map<String, String>> getSort() {
        return sort;
    }

    public List<String> getReturnedMeasureAttributes() {
        return returnedMeasureAttributes;
    }

    public String getAggregations() {
        return aggregations;
    }

    public Long getComponentRowLimit() {
        return componentRowLimit;
    }

    public Long getComponentRowStart() {
        return componentRowStart;
    }
}
