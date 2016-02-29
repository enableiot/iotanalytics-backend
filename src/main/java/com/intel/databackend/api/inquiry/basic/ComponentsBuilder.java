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

import com.intel.databackend.api.inquiry.basic.validators.ComponentsDataTypeValidator;
import com.intel.databackend.api.inquiry.samples.SampleDataRetriever;
import com.intel.databackend.datastructures.Component;
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.exceptions.ErrorMsg;
import com.intel.databackend.exceptions.IllegalDataInquiryArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComponentsBuilder {

    private final ComponentsDataTypeValidator componentsDataTypeValidator;
    private final SampleDataRetriever sampleDataRetriever;

    public ComponentsBuilder(ComponentsDataTypeValidator componentsDataTypeValidator,
                             SampleDataRetriever sampleDataRetriever) {
        this.componentsDataTypeValidator = componentsDataTypeValidator;
        this.sampleDataRetriever = sampleDataRetriever;
    }

    public List<Component> build(Map<String, Observation[]> componentObservations)
            throws IllegalDataInquiryArgumentException {
        List<Component> components = new ArrayList<>();
        for (Map.Entry<String, Observation[]> entry : componentObservations.entrySet()) {
            String componentId = entry.getKey();

            if (!componentsDataTypeValidator.isValid(componentId)) {
                throw new IllegalDataInquiryArgumentException(ErrorMsg.DATA_TYPE_NOT_SUPPORTED_FOR_BUCKETING,
                        ErrorMsg.DEFAULT_ERROR_CODE);
            }

            Component component = new Component();
            component.setComponentId(componentId);
            long endIndex = entry.getValue().length;
            long startIndex = 0;
            component.setSamples(sampleDataRetriever.get(entry.getValue(), startIndex, endIndex));
            components.add(component);
        }

        return components;
    }


}
