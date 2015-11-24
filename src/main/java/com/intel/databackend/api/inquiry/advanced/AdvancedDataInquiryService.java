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
import com.intel.databackend.api.helpers.ComponentFilters;
import com.intel.databackend.api.inquiry.DataRetrieveParams;
import com.intel.databackend.api.inquiry.DataRetriever;
import com.intel.databackend.api.inquiry.advanced.componentsbuilder.AdvancedComponentsBuilder;
import com.intel.databackend.api.inquiry.advanced.componentsbuilder.ComponentsBuilderParams;
import com.intel.databackend.api.inquiry.advanced.filters.AdvancedObservationFilterSelector;
import com.intel.databackend.api.inquiry.advanced.filters.ObservationFilterSelector;
import com.intel.databackend.datasources.dashboard.components.ComponentsDao;
import com.intel.databackend.datasources.hbase.DataDao;
import com.intel.databackend.datasources.dashboard.devices.DeviceDao;
import com.intel.databackend.datasources.dashboard.utils.QueryFields;
import com.intel.databackend.datasources.dashboard.utils.QueryParam;
import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.DeviceData;
import com.intel.databackend.datastructures.requests.AdvDataInquiryRequest;
import com.intel.databackend.datastructures.responses.AdvDataInquiryResponse;
import com.intel.databackend.exceptions.DataInquiryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

public class AdvancedDataInquiryService implements Service<AdvDataInquiryRequest, AdvDataInquiryResponse> {
    private String accountId;
    private AdvDataInquiryRequest dataInquiryRequest;
    private ComponentFilters componentFilters;

    private static final Logger logger = LoggerFactory.getLogger(AdvancedDataInquiryService.class);

    private DataDao hbase;
    private final ComponentsDao componentsDao;
    private final DeviceDao deviceDao;

    private DataRetriever dataRetriever;
    private DataRetrieveParams dataRetrieveParams;

    private ResponseBuilder responseBuilder;
    private ObservationFilterSelector observationFilterSelector;

    @Autowired
    public AdvancedDataInquiryService(DataDao hbase, ComponentsDao componentsDao, DeviceDao deviceDao) {
        this.hbase = hbase;
        this.componentsDao = componentsDao;
        this.deviceDao = deviceDao;
    }

    @Override
    public AdvancedDataInquiryService withParams(String accountId, AdvDataInquiryRequest request) {
        this.accountId = accountId;
        this.dataInquiryRequest = request;
        this.dataRetrieveParams = new DataRetrieveParams(this.dataInquiryRequest, this.accountId);
        dataRetriever = new DataRetriever(hbase, dataRetrieveParams);
        observationFilterSelector = new AdvancedObservationFilterSelector(dataInquiryRequest);
        componentFilters = new ComponentFilters(componentsDao, dataInquiryRequest.getDevCompAttributeFilter());
        return this;
    }

    @Override
    public AdvDataInquiryResponse invoke() throws DataInquiryException {

        List<String> componentIds = getRequestedComponentIds();

        responseBuilder = new ResponseBuilder(dataInquiryRequest, accountId);

        if (componentIds != null && componentIds.size() > 0) {

            Map<String, ComponentDataType> componentsMetadata = componentsDao.getAdvComponentsMetadata(accountId, componentIds);
            componentIds = filterRequestedComponentIds(componentIds, componentsMetadata);

            if (componentIds != null && componentIds.size() > 0) {
                String[] measureAttributes;
                if (dataInquiryRequest.hasRequestMeasuredAttributes()) {
                    measureAttributes = getAttributesFromRequest();
                } else {
                    measureAttributes = getAllAttributes(componentIds);
                }

                dataRetrieveParams.setComponents(componentIds);
                dataRetrieveParams.setComponentsAttributes(Arrays.asList(measureAttributes));
                dataRetrieveParams.setComponentsMetadata(componentsMetadata);
                dataRetriever.retrieveAndCount(observationFilterSelector);

                List<DeviceData> devicesData = deviceDao.getDevicesFromAccount(accountId, componentIds);

                if (!dataInquiryRequest.isCountOnly()) {
                    ComponentsBuilderParams parameters = new ComponentsBuilderParams(dataInquiryRequest, measureAttributes);
                    AdvancedComponentsBuilder advancedComponentsBuilder = new AdvancedComponentsBuilder(componentsMetadata, dataRetriever.getComponentObservations());
                    advancedComponentsBuilder.appendComponentsDetails(devicesData, parameters);
                }

                responseBuilder.build(dataRetriever.getRowCount(), devicesData);
            }
        }

        return responseBuilder.getDataInquiryResponse();
    }

    private String[] getAllAttributes(List<String> componentIds) {
        Set<String> allAttributes = new HashSet<>();
        for (String componentId : componentIds) {
            try {
                String[] attributeNames = hbase.scanForAttributeNames(accountId, componentId, dataInquiryRequest.getStartTimestamp(), dataInquiryRequest.getEndTimestamp());
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

    private List<String> getRequestedComponentIds() {
        List<String> componentIds = dataInquiryRequest.getComponentIds();

        List<String> accountsComponents = componentsDao.getComponentsByCustomParams(accountId, createFilters());

        //If there are no components in request use components from database
        if (dataInquiryRequest.hasEmptyComponentsIds()) {
            componentIds = accountsComponents;
        } else {
            if (accountsComponents != null) {
                componentIds.retainAll(accountsComponents);
            }
        }

        logger.debug("Components selected: {}", componentIds);
        return componentIds;
    }

    private List<QueryParam> createFilters() {
        List<QueryParam> filters = new ArrayList<>();

        if (dataInquiryRequest.getDeviceIds() != null && dataInquiryRequest.getDeviceIds().size() > 0) {
            filters.add(new QueryParam(QueryFields.DEVICE_ID, dataInquiryRequest.getDeviceIds()));
        }
        if (dataInquiryRequest.getGatewayIds() != null && dataInquiryRequest.getGatewayIds().size() > 0) {
            filters.add(new QueryParam(QueryFields.DEVICE_GATEWAY, dataInquiryRequest.getGatewayIds()));
        }

        //Select all components from account, if there are no filters in request
        if (filters.isEmpty()) {
            List<String> accountIdAsList = new ArrayList<String>();
            accountIdAsList.add(accountId);
            filters.add(new QueryParam(QueryFields.ACCOUNT_ID, accountIdAsList));
        }

        return filters;
    }

    private List<String> filterRequestedComponentIds(List<String> componentIds, Map<String, ComponentDataType> componentsMetadata) {
        if (componentFilters.isFilterAvailable()) {
            return componentFilters.filter(componentsMetadata, componentIds, accountId);
        }
        return componentIds;
    }
}