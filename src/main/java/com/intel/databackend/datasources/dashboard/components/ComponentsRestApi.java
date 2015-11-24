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

package com.intel.databackend.datasources.dashboard.components;

import com.intel.databackend.config.DashboardCredentialsProvider;
import com.intel.databackend.datasources.dashboard.auth.AuthApi;
import com.intel.databackend.datasources.dashboard.utils.QueryFields;
import com.intel.databackend.datasources.dashboard.utils.QueryParam;
import com.intel.databackend.datasources.dashboard.utils.RestApiHelper;
import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.DeviceComponent;
import com.intel.databackend.datastructures.requests.ComponentSearchRequest;
import com.intel.databackend.datastructures.requests.DeviceSearchRequest;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ComponentsRestApi implements ComponentsDao {

    private static final Logger logger = LoggerFactory.getLogger(ComponentsRestApi.class);

    private final String url;
    private final AuthApi authApi;
    private final static String PATH = "/v1/api/accounts/{accountId}/devices/components";
    private String token;

    @Autowired
    public ComponentsRestApi(DashboardCredentialsProvider dashboardEndpointConfig, AuthApi authApi) throws VcapEnvironmentException {
        this.authApi = authApi;
        url = dashboardEndpointConfig.getEndpoint() + PATH;
        dashboardEndpointConfig.readEndpoint();
    }

    @Override
    public Map<String, ComponentDataType> getAdvComponentsMetadata(String accountId, List<String> componentIds) {
        List<QueryParam> queryParameters = new ArrayList<QueryParam>();
        filterByAccountId(accountId, queryParameters);
        queryParameters.add(new QueryParam(QueryFields.COMPONENT_ID, componentIds));
        return getComponentsByDeviceProperties(queryParameters);
    }

    @Override
    public List<String> getComponentsFromAccount(String accountId) {
        return getComponentIdsFromAccount(accountId);
    }

    @Override
    public List<String> getComponentsByCustomParams(String accountId, List<QueryParam> queryParameters) {
        filterByAccountId(accountId, queryParameters);
        return getComponentIdsByDeviceProperties(queryParameters);
    }

    private void filterByAccountId(String accountId, List<QueryParam> queryParameters) {
        List<String> accountIdList = new ArrayList<>();
        accountIdList.add(accountId);
        queryParameters.add(new QueryParam(QueryFields.ACCOUNT_ID, accountIdList));
    }

    private List<String> getComponentIdsFromAccount(String accountId) {
        List<String> components = new ArrayList<>();

        RestTemplate template = new RestTemplate();

        HttpHeaders headers = RestApiHelper.getHttpHeaders(getToken());
        HttpEntity<DeviceSearchRequest> req = new HttpEntity<>(null, headers);

        ParameterizedTypeReference<List<DeviceComponent>> responseType = new ParameterizedTypeReference<List<DeviceComponent>>() {};
        ResponseEntity<List<DeviceComponent>> resp = template.exchange(url.replace("{accountId}", accountId), HttpMethod.GET, req, responseType);

        if(resp.getStatusCode().equals(HttpStatus.OK)) {
            for (DeviceComponent comp : resp.getBody()) {
                components.add(comp.getCid());
            }
        } else {
            logger.warn("Wrong response code from dashboard: {}", resp.getStatusCode());
        }

        return components;
    }

    private void addSearchParamToBody(QueryParam param, ComponentSearchRequest body) {
        String fieldName = param.getFieldName();
        List<String> values = param.getValues();
        if (fieldName.equals(QueryFields.DEVICE_ID.toString())) {
            body.setDeviceIds(values);
        } else if (fieldName.equals(QueryFields.DEVICE_NAME.toString())) {
            body.setDeviceNames(values);
        } else if (fieldName.equals(QueryFields.DEVICE_GATEWAY.toString())) {
            body.setGatewayIds(values);
        } else if (fieldName.equals(QueryFields.DEVICE_TAG.toString())) {
            body.setDeviceTags(values);
        } else if (fieldName.equals(QueryFields.COMPONENT_ID.toString())) {
            body.setComponentIds(values);
        }
    }

    private List<String> getComponentIdsByDeviceProperties(List<QueryParam> queryParameters) {
        List<String> components = new ArrayList<>();

        RestTemplate template = new RestTemplate();
        ComponentSearchRequest body = new ComponentSearchRequest();

        HttpHeaders headers = RestApiHelper.getHttpHeaders(getToken());
        HttpEntity<ComponentSearchRequest> req = new HttpEntity<ComponentSearchRequest>(body, headers);

        String accountId = null;
        for (QueryParam param : queryParameters) {
            logger.debug("QUERY PARAM: {} = {}", param.getFieldName(), param.getValues());
            if (param.getFieldName().equals(QueryFields.ACCOUNT_ID.toString())) {
                accountId = param.getValues().get(0);
            } else {
                addSearchParamToBody(param, body);
            }
        }
        ParameterizedTypeReference<List<DeviceComponent>> responseType = new ParameterizedTypeReference<List<DeviceComponent>>() {};
        ResponseEntity<List<DeviceComponent>> resp = template.exchange(url.replace("{accountId}", accountId), HttpMethod.POST, req, responseType);

        if(resp.getStatusCode().equals(HttpStatus.OK)) {
            for (DeviceComponent comp : resp.getBody()) {
                String cid = comp.getCid();
                logger.debug("Adding cid {} to list", cid);
                components.add(cid);
            }
        } else {
            logger.warn("Wrong response code from dashboard: {}", resp.getStatusCode());
        }

        return components;
    }

    private Map<String, ComponentDataType> getComponentsByDeviceProperties(List<QueryParam> queryParameters) {
        Map<String, ComponentDataType> components = new HashMap<String, ComponentDataType>();

        RestTemplate template = new RestTemplate();
        ComponentSearchRequest body = new ComponentSearchRequest();

        String accountId = null;
        for (QueryParam param : queryParameters) {
            logger.debug("QUERY PARAM: {} = {}", param.getFieldName(), param.getValues());
            if (param.getFieldName().equals(QueryFields.ACCOUNT_ID.toString())) {
                accountId = param.getValues().get(0);
            } else {
                addSearchParamToBody(param, body);
            }
        }

        HttpHeaders headers = RestApiHelper.getHttpHeaders(getToken());
        HttpEntity<ComponentSearchRequest> req = new HttpEntity<ComponentSearchRequest>(body, headers);


        ParameterizedTypeReference<List<DeviceComponent>> responseType = new ParameterizedTypeReference<List<DeviceComponent>>() {};
        ResponseEntity<List<DeviceComponent>> resp = template.exchange(url.replace("{accountId}", accountId), HttpMethod.POST, req, responseType);

        if(resp.getStatusCode().equals(HttpStatus.OK)) {
            for (DeviceComponent comp : resp.getBody()) {
                ComponentDataType dataType = new ComponentDataType();
                dataType.setComponentId(comp.getCid());
                dataType.setComponentName(comp.getName());
                dataType.setComponentType(comp.getComponentType().getId());
                dataType.setDataType(comp.getComponentType().getDataType());
                dataType.setFormat(comp.getComponentType().getFormat());
                components.put(comp.getCid(), dataType);
            }
        } else {
            logger.warn("Wrong response code from dashboard: {}", resp.getStatusCode());
        }

        return components;
    }

    private String getToken() {
        if (token == null) {
            token = authApi.getUserToken();
        }
        return token;
    }

}
