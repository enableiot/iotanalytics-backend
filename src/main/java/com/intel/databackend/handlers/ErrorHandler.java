package com.intel.databackend.handlers;

import com.intel.databackend.datastructures.responses.ErrorResponse;
import com.intel.databackend.exceptions.DataInquiryException;
import com.intel.databackend.exceptions.MissingDataSubmissionArgumentException;
import com.intel.databackend.exceptions.ServiceException;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2015 Intel Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler(DataInquiryException.class)
    public ResponseEntity handleError(DataInquiryException ex) {
        return new ResponseEntity<ErrorResponse>(ex.getResponse(), ex.getHttpErrorStatus());
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity handleError(ServiceException ex) {
        logger.error("Unable to read dashboard api url", ex);
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(VcapEnvironmentException.class)
    public ResponseEntity handleError(VcapEnvironmentException ex) {
        logger.error("Unable to parse Cloud Foundry env variables", ex);
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MissingDataSubmissionArgumentException.class)
    public ResponseEntity handleError(MissingDataSubmissionArgumentException ex) {
        logger.error("Bad request: ", ex);
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity handleError(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        logger.error("Validation error: ", errors);
        return new ResponseEntity<Map<String, String>>(errors, HttpStatus.BAD_REQUEST);
    }
}

