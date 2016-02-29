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

import com.intel.databackend.api.Service;
import com.intel.databackend.api.inquiry.DataRetrieveParams;
import com.intel.databackend.api.inquiry.DataRetriever;
import com.intel.databackend.api.inquiry.advanced.componentsbuilder.AdvancedComponentsBuilder;
import com.intel.databackend.api.inquiry.advanced.componentsbuilder.ComponentsBuilderParams;
import com.intel.databackend.api.inquiry.advanced.filters.AdvancedObservationFilterSelector;
import com.intel.databackend.api.inquiry.advanced.filters.ObservationFilterSelector;
import com.intel.databackend.datasources.hbase.DataDao;
import com.intel.databackend.datastructures.AdvancedComponent;
import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.DeviceData;
import com.intel.databackend.datastructures.requests.AdvDataInquiryRequest;
import com.intel.databackend.datastructures.responses.AdvDataInquiryResponse;
import com.intel.databackend.exceptions.DataInquiryException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

@org.springframework.stereotype.Service
@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
public class AdvancedDataInquiryService implements Service<AdvDataInquiryRequest, AdvDataInquiryResponse> {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedDataInquiryService.class);
    private String accountId;
    private AdvDataInquiryRequest dataInquiryRequest;
    private DataDao hbase;
    private DataRetriever dataRetriever;
    private DataRetrieveParams dataRetrieveParams;

    private ObservationFilterSelector observationFilterSelector;

    @Autowired
    public AdvancedDataInquiryService(DataDao hbase) {
        this.hbase = hbase;
    }

    @Override
    public AdvancedDataInquiryService withParams(String accountId, AdvDataInquiryRequest request) {
        this.accountId = accountId;
        this.dataInquiryRequest = request;
        this.dataRetrieveParams = new DataRetrieveParams(this.dataInquiryRequest, this.accountId);
        dataRetriever = new DataRetriever(hbase, dataRetrieveParams);
        observationFilterSelector = new AdvancedObservationFilterSelector(dataInquiryRequest);
        return this;
    }

    @Override
    public AdvDataInquiryResponse invoke() throws DataInquiryException {
        ResponseBuilder responseBuilder = new ResponseBuilder(dataInquiryRequest, accountId);
        List<DeviceData> devicesData = new ArrayList<>(dataInquiryRequest.getDeviceDataList());
        Map<String, ComponentDataType> componentsMetadata = fetchComponentDataType(devicesData);

        if (MapUtils.isNotEmpty(componentsMetadata)) {
            String[] measureAttributes;
            if (dataInquiryRequest.hasRequestMeasuredAttributes()) {
                measureAttributes = getAttributesFromRequest();
            } else {
                measureAttributes = getAllAttributes(componentsMetadata.keySet());
            }

            dataRetrieveParams.setComponentsAttributes(Arrays.asList(measureAttributes));
            dataRetrieveParams.setComponentsMetadata(componentsMetadata);
            dataRetriever.retrieveAndCount(observationFilterSelector);

            if (!dataInquiryRequest.isCountOnly()) {
                ComponentsBuilderParams parameters = new ComponentsBuilderParams(dataInquiryRequest, measureAttributes);
                AdvancedComponentsBuilder advancedComponentsBuilder =
                        new AdvancedComponentsBuilder(componentsMetadata, dataRetriever.getComponentObservations());
                advancedComponentsBuilder.appendComponentsDetails(devicesData, parameters);
            }

            responseBuilder.build(dataRetriever.getRowCount(), devicesData);
        }
        return responseBuilder.getDataInquiryResponse();
    }

    private Map<String, ComponentDataType> fetchComponentDataType(List<DeviceData> deviceDataList) {
        Map<String, ComponentDataType> result = new HashMap<>();
        for (DeviceData deviceData : deviceDataList) {
            for (AdvancedComponent component : deviceData.getComponents()) {
                ComponentDataType componentDataType = new ComponentDataType();
                componentDataType.setComponentId(component.getComponentId());
                componentDataType.setComponentName(component.getComponentName());
                componentDataType.setComponentType(component.getComponentType());
                componentDataType.setDataType(component.getDataType());
                if (StringUtils.isNotEmpty(component.getComponentId())) {
                    result.put(component.getComponentId(), componentDataType);
                }
            }
        }
        return result;
    }

    private String[] getAllAttributes(Collection<String> componentIds) {
        Set<String> allAttributes = new HashSet<>();
        for (String componentId : componentIds) {
            try {
                String[] attributeNames = hbase.scanForAttributeNames(accountId, componentId,
                        dataInquiryRequest.getStartTimestamp(), dataInquiryRequest.getEndTimestamp());

                for (String name : attributeNames) {
                    allAttributes.add(name);
                }
            } catch (IOException e) {
                logger.warn("an exception was thrown during attribute names retrieve phase: ", e);
            }
        }
        return allAttributes.toArray(new String[allAttributes.size()]);
    }

    private String[] getAttributesFromRequest() {
        Set<String> attributesSet = new HashSet<>();
        if (dataInquiryRequest.getReturnedMeasureAttributes() != null) {
            attributesSet.addAll(dataInquiryRequest.getReturnedMeasureAttributes());
        }
        if (dataInquiryRequest.getMeasurementAttributeFilter() != null) {
            attributesSet.addAll(dataInquiryRequest.getMeasurementAttributeFilter().keySet());
        }
        return attributesSet.toArray(new String[attributesSet.size()]);
    }
}