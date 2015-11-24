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

package com.intel.databackend.datasources.dashboard.auth;

import com.intel.databackend.config.DashboardCredentialsProvider;
import com.intel.databackend.datastructures.requests.AuthTokenRequest;
import com.intel.databackend.datastructures.responses.AuthTokenResponse;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class AuthRestApi implements AuthApi {

    private static final Logger logger = LoggerFactory.getLogger(AuthRestApi.class);

    private final DashboardCredentialsProvider dashboardEndpointConfig;
    private final String url;
    private static final String PATH = "/v1/api/auth/token";

    private String systemUserToken;

    @Autowired
    public AuthRestApi(DashboardCredentialsProvider dashboardEndpointConfig) throws VcapEnvironmentException {
        this.dashboardEndpointConfig = dashboardEndpointConfig;
        dashboardEndpointConfig.readEndpoint();
        url = dashboardEndpointConfig.getEndpoint() + PATH;
    }

    @Override
    public String getUserToken() {
        if (systemUserToken != null) {
            logger.info("Token cached");
            return systemUserToken;
        }
        RestTemplate template = new RestTemplate();
        AuthTokenRequest body = new AuthTokenRequest();
        body.setUsername(dashboardEndpointConfig.getUsername());
        body.setPassword(dashboardEndpointConfig.getPassword());
        AuthTokenResponse resp = template.postForObject(url, body, AuthTokenResponse.class);
        logger.info("Token retrieved");
        systemUserToken = resp.getToken();
        return systemUserToken;
    }
}
