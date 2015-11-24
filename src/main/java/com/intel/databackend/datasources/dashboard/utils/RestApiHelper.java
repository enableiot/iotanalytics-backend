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

package com.intel.databackend.datasources.dashboard.utils;

import org.springframework.http.HttpHeaders;

public final class RestApiHelper {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private RestApiHelper() {}

    public static HttpHeaders getHttpHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, getBearer(token));
        return headers;
    }

    private static String getBearer(String token) {
        StringBuilder sb = new StringBuilder().append(BEARER).append(token);
        return sb.toString();
    }

}
