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

package com.intel.databackend.handlers;

import com.intel.databackend.api.Service;
import com.intel.databackend.datastructures.requests.AdvDataInquiryRequest;
import com.intel.databackend.datastructures.requests.DataInquiryRequest;
import com.intel.databackend.datastructures.requests.DataSubmissionRequest;
import com.intel.databackend.datastructures.requests.FirstLastTimestampRequest;
import com.intel.databackend.datastructures.responses.AdvDataInquiryResponse;
import com.intel.databackend.datastructures.responses.DataInquiryResponse;
import com.intel.databackend.datastructures.responses.DataSubmissionResponse;
import com.intel.databackend.datastructures.responses.FirstLastTimestampResponse;
import com.intel.databackend.exceptions.ServiceException;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import com.intel.databackend.handlers.requestvalidator.AdvanceDataRequestValidator;
import com.intel.databackend.handlers.requestvalidator.DataRequestValidator;
import com.intel.databackend.handlers.requestvalidator.RequestValidator;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@ControllerAdvice
public class Data {

    private static final Logger logger = LoggerFactory.getLogger(Data.class);

    @Autowired
    @Qualifier("basicDataInquiryService")
    private Service<DataInquiryRequest, DataInquiryResponse> basicDataInquiryService;
    @Autowired
    @Qualifier("advancedDataInquiryService")
    private Service<AdvDataInquiryRequest, AdvDataInquiryResponse> advancedDataInquiryService;
    @Autowired
    @Qualifier("dataSubmissionService")
    private Service<DataSubmissionRequest, DataSubmissionResponse> dataSubmissionService;
    @Autowired
    @Qualifier("firstLastTimestampService")
    private Service<FirstLastTimestampRequest, FirstLastTimestampResponse> firstLastTimestampService;

    private RequestValidator requestValidator;

    @RequestMapping(value="/v1/accounts/{accountId}/dataSubmission", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity dataSubmission(@PathVariable String accountId, @Valid @RequestBody DataSubmissionRequest request,
                                                       BindingResult result) throws ServiceException, BindException {
        logger.info("REQUEST: aid: {}", accountId);
        logger.debug("{}", request);

        if (result.hasErrors()) {
            throw new BindException(result);
        } else {
            dataSubmissionService.withParams(accountId, request);

            dataSubmissionService.invoke();
            ResponseEntity res = new ResponseEntity<>(HttpStatus.CREATED);
            logger.info("RESPONSE: {}", res.getStatusCode());
            return res;
        }
    }

    @RequestMapping(value="/v1/accounts/{accountId}/dataInquiry", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity dataInquiry(@PathVariable String accountId, @RequestBody DataInquiryRequest request) throws ServiceException, VcapEnvironmentException {
        logger.info("REQUEST: aid: {}", accountId);
        logger.debug("{}",request);

        requestValidator = new DataRequestValidator(request);
        requestValidator.validate();

        basicDataInquiryService.withParams(accountId, request);
        DataInquiryResponse dataInquiryResponse = basicDataInquiryService.invoke();

        ResponseEntity res = new ResponseEntity<DataInquiryResponse>(dataInquiryResponse, HttpStatus.OK);
        logger.info("RESPONSE: {}", res.getStatusCode());
        logger.debug("{}", dataInquiryResponse);
        return res;
    }

    @RequestMapping(value="/v1/accounts/{accountId}/dataInquiry/advanced", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity advancedDataInquiry(@PathVariable String accountId, @RequestBody final AdvDataInquiryRequest request) throws ServiceException, VcapEnvironmentException {
        logger.info("REQUEST: aid: {}", accountId);
        logger.debug("{}", request);

        requestValidator = new AdvanceDataRequestValidator(request);
        requestValidator.validate();

        advancedDataInquiryService.withParams(accountId, request);
        AdvDataInquiryResponse dataInquiryResponse = advancedDataInquiryService.invoke();

        ResponseEntity res = new ResponseEntity<AdvDataInquiryResponse>(dataInquiryResponse, HttpStatus.OK);
        logger.info("RESPONSE: {}", res.getStatusCode());
        logger.debug("{}", dataInquiryResponse);
        return res;
    }

    @RequestMapping(value="/v1/accounts/{accountId}/inquiryComponentFirstAndLast")
    public @ResponseBody ResponseEntity firstLastMeasurementTimestamp(@PathVariable String accountId,
                                                                      @Valid @RequestBody final FirstLastTimestampRequest request,
                                                                      BindingResult result) throws VcapEnvironmentException, ServiceException, BindException {
        logger.info("REQUEST: aid: {}", accountId);
        logger.debug(request.toString());

        if (result.hasErrors()) {
            throw new BindException(result);
        } else {
            firstLastTimestampService.withParams(accountId, request);
            FirstLastTimestampResponse firstLastTimestampResponse = firstLastTimestampService.invoke();
            ResponseEntity res = new ResponseEntity<>(firstLastTimestampResponse, HttpStatus.OK);
            logger.info("RESPONSE: {}", res.getStatusCode());
            logger.debug("{}", firstLastTimestampResponse);
            return res;
        }
    }

    @RequestMapping("/")
    @ResponseBody
    public String index() throws JSONException {
        return this.version();
    }

    @RequestMapping("/v1/version")
    @ResponseBody
    public String version() throws JSONException {
        JSONObject appVersion = new JSONObject();
        appVersion.put("version", System.getenv("VERSION"));
        appVersion.put("name", "installer-backend");
        return appVersion.toString();
    }
}
