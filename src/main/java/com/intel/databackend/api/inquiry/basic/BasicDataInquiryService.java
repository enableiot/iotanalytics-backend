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

import com.intel.databackend.api.Service;
import com.intel.databackend.api.inquiry.DataRetrieveParams;
import com.intel.databackend.api.inquiry.DataRetriever;
import com.intel.databackend.api.inquiry.advanced.filters.BaseObservationFilterSelector;
import com.intel.databackend.api.inquiry.advanced.filters.ObservationFilterSelector;
import com.intel.databackend.api.inquiry.basic.validators.BucketDataTypeValidator;
import com.intel.databackend.api.inquiry.basic.validators.ComponentsDataTypeValidator;
import com.intel.databackend.api.inquiry.basic.validators.PlainDataTypeValidator;
import com.intel.databackend.api.inquiry.samples.SampleAggregationDataRetriever;
import com.intel.databackend.api.inquiry.samples.SampleDataRetriever;
import com.intel.databackend.api.inquiry.samples.SamplePlainDataRetriever;
import com.intel.databackend.datasources.hbase.DataDao;
import com.intel.databackend.datastructures.Component;
import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.requests.DataInquiryRequest;
import com.intel.databackend.datastructures.responses.DataInquiryResponse;
import com.intel.databackend.exceptions.DataInquiryException;
import com.intel.databackend.exceptions.IllegalDataInquiryArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Map;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

@org.springframework.stereotype.Service
@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
public class BasicDataInquiryService implements Service<DataInquiryRequest, DataInquiryResponse> {

    private static final Logger logger = LoggerFactory.getLogger(BasicDataInquiryService.class);

    private String accountId;
    private DataInquiryRequest dataInquiryRequest;

    private DataDao hbase;

    private Map<String, ComponentDataType> componentsMetadata;

    private DataRetriever dataRetriever;

    private DataRetrieveParams dataRetrieveParams;

    private List<Component> outputComponents;

    private ObservationFilterSelector observationFilterSelector;

    @Autowired
    public BasicDataInquiryService(DataDao hbase) {
        this.hbase = hbase;
    }

    @Override
    public BasicDataInquiryService withParams(String accountId, DataInquiryRequest request) {
        this.accountId = accountId;
        this.dataInquiryRequest = request;
        this.dataRetrieveParams = new DataRetrieveParams(this.dataInquiryRequest, this.accountId);
        this.componentsMetadata = request.getComponentsWithDataType();
        this.dataRetriever = new DataRetriever(hbase, dataRetrieveParams);
        this.observationFilterSelector = new BaseObservationFilterSelector();
        return this;
    }

    @Override
    public DataInquiryResponse invoke() throws DataInquiryException {
        try {
            dataRetriever.retrieveAndCount(observationFilterSelector);

            if (!isCountOnly()) {
                createComponents();
            }

            return buildOutputMessage();
        } catch (IllegalDataInquiryArgumentException ex) {
            logger.warn("Wrong data inquiry parameter", ex);
            throw new DataInquiryException(ex);
        }
    }

    private void createComponents() throws IllegalDataInquiryArgumentException {
        SampleDataRetriever sampleDataRetriever;
        ComponentsDataTypeValidator componentsDataTypeValidator;

        if (!shouldGenerateAggregations()) {
            sampleDataRetriever = new SamplePlainDataRetriever(null);
            componentsDataTypeValidator = new PlainDataTypeValidator();
        } else {
            sampleDataRetriever = new SampleAggregationDataRetriever(dataRetrieveParams);
            componentsDataTypeValidator = new BucketDataTypeValidator(componentsMetadata);
        }

        ComponentsBuilder componentsBuilder = new ComponentsBuilder(componentsDataTypeValidator, sampleDataRetriever);
        outputComponents = componentsBuilder.build(dataRetriever.getComponentObservations());
    }

    private DataInquiryResponse buildOutputMessage() {
        ResponseBuilder responseBuilder = new ResponseBuilder(accountId, dataRetriever.getRowCount(), outputComponents);
        return responseBuilder.getDataInquiryResponse();
    }

    private boolean isCountOnly() {
        return dataInquiryRequest.getCountOnly() != null && dataInquiryRequest.getCountOnly();
    }

    private boolean shouldGenerateAggregations() {
        return dataInquiryRequest.getMaxPoints() != null && dataInquiryRequest.getMaxPoints() > 0;
    }
}
