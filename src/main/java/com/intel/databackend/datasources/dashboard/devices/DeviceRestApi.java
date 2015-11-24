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

package com.intel.databackend.datasources.dashboard.devices;

import com.intel.databackend.config.DashboardCredentialsProvider;
import com.intel.databackend.datasources.dashboard.auth.AuthApi;
import com.intel.databackend.datasources.dashboard.utils.RestApiHelper;
import com.intel.databackend.datastructures.AdvancedComponent;
import com.intel.databackend.datastructures.DeviceComponent;
import com.intel.databackend.datastructures.DeviceData;
import com.intel.databackend.datastructures.DeviceSearchCriterium;
import com.intel.databackend.datastructures.requests.DeviceSearchRequest;
import com.intel.databackend.datastructures.responses.DeviceSearchResponse;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class DeviceRestApi implements DeviceDao{

    private static final Logger logger = LoggerFactory.getLogger(DeviceRestApi.class);

    private final String url;
    private AuthApi authApi;
    private static final String PATH = "/v1/api/accounts/{accountId}/devices/search";

    @Autowired
    public DeviceRestApi(DashboardCredentialsProvider dashboardEndpointConfig, AuthApi authApi) throws VcapEnvironmentException {
        dashboardEndpointConfig.readEndpoint();
        url = dashboardEndpointConfig.getEndpoint() + PATH;
        this.authApi = authApi;
    }

    public List<DeviceData> getDevicesFromAccount(String accountId, List<String> componentIds) {
        List<DeviceData> devices = new ArrayList<>();

        RestTemplate template = new RestTemplate();
        DeviceSearchRequest body = new DeviceSearchRequest();
        onlyActive(body);
        HttpHeaders headers = RestApiHelper.getHttpHeaders(authApi.getUserToken());
        HttpEntity<DeviceSearchRequest> req = new HttpEntity<>(body, headers);

        ParameterizedTypeReference<List<DeviceSearchResponse>> responseType = new ParameterizedTypeReference<List<DeviceSearchResponse>>() {
        };

        ResponseEntity<List<DeviceSearchResponse>> resp = template.exchange(url.replace("{accountId}", accountId), HttpMethod.POST, req, responseType);
        if (resp.getStatusCode().equals(HttpStatus.OK)) {
            List<DeviceSearchResponse> searchResponseList = resp.getBody();
            for (DeviceSearchResponse device : searchResponseList) {
                DeviceData devData = new DeviceData();
                devData.setComponents(new ArrayList<>());
                for (DeviceComponent comp : device.getComponents()) {
                    if (componentIds.contains(comp.getCid())) {
                        AdvancedComponent c = new AdvancedComponent();
                        c.setComponentId(comp.getCid());
                        c.setComponentName(comp.getName());
                        c.setComponentType(comp.getComponentType().getId());
                        devData.getComponents().add(c);
                    }
                }
                if (!devData.getComponents().isEmpty()) {
                    devData.setDeviceId(device.getDeviceId());
                    devData.setDeviceName(device.getName());
                    devData.setAccountId(accountId);
                    devices.add(devData);
                }
            }
        } else {
            logger.warn("Wrong response code from dashboard: {}", resp.getStatusCode());
        }
        return devices;
    }

    private void onlyActive(DeviceSearchRequest body) {
        DeviceSearchCriterium status = new DeviceSearchCriterium();
        status.setOperator("eq");
        status.setValue(new ArrayList<>());
        status.getValue().add("active");
        body.setStatus(status);
    }
}
