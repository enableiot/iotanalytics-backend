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

package com.intel.databackend.exceptions;

import com.intel.databackend.datastructures.ErrorElement;
import com.intel.databackend.datastructures.responses.ErrorResponse;
import org.springframework.http.HttpStatus;


public class DataInquiryException extends ServiceException {

    public static final int DEFAULT_ERROR_CODE = 400;

    private int errorCode;

    public ErrorResponse getResponse() {
        ErrorResponse err = new ErrorResponse();

        err.getErrors().add(new ErrorElement(getErrorCode(), getMessage()));
        return err;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public DataInquiryException(String message) {
        super(message);
        errorCode = DEFAULT_ERROR_CODE;
    }

    public DataInquiryException(String message, Throwable cause) {
        super(message, cause);
        errorCode = DEFAULT_ERROR_CODE;
    }

    public DataInquiryException(IllegalDataInquiryArgumentException cause) {
        super(cause.getMessage(), cause);
        errorCode = cause.getErrorCode();
    }

    public HttpStatus getHttpErrorStatus() {
        return HttpStatus.valueOf(getErrorCode());
    }
}
