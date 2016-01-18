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

package com.intel.databackend.config.cloudfoundry;

import com.intel.databackend.config.cloudfoundry.utils.VcapReader;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DashboardEndpointConfigTest {

    @Mock
    private VcapReader vcapReaderServices;

    @InjectMocks
    private DashboardEndpointConfig dashboardEndpointConfig;

    private final static String host = "intel.com";
    private final static String user = "James";
    private final static String password = "pass";

    @Before
    public void init() throws JSONException {
        Mockito.when(vcapReaderServices.getUserProvidedServiceCredentialsByName(DashboardEndpointConfig.ENDPOINT_SERVICE_NAME))
                .thenReturn(new JSONObject("{\"" + DashboardEndpointConfig.HOST + "\": \"" + host + "\"}"));
        Mockito.when(vcapReaderServices.getUserProvidedServiceCredentialsByName(DashboardEndpointConfig.USER_SERVICE_NAME))
                .thenReturn(new JSONObject("{\"" +
                        DashboardEndpointConfig.USERNAME + "\": \"" + user + "\", \"" +
                        DashboardEndpointConfig.PASSWORD + "\": \"" + password + "\"}"));
    }

    @Test
    public void testGetMethods() throws JSONException, VcapEnvironmentException {
        dashboardEndpointConfig.readEndpoint();
        Assert.assertEquals(dashboardEndpointConfig.getEndpoint(), host);
        Assert.assertEquals(dashboardEndpointConfig.getUsername(), user);
        Assert.assertEquals(dashboardEndpointConfig.getPassword(), password);
    }

    @Test(expected = VcapEnvironmentException.class)
    public void testReadEndpoint_empty_endpoint_service_error() throws JSONException, VcapEnvironmentException {
        Mockito.when(vcapReaderServices.getUserProvidedServiceCredentialsByName(DashboardEndpointConfig.ENDPOINT_SERVICE_NAME))
                .thenReturn(null);
        dashboardEndpointConfig.readEndpoint();
    }

    @Test(expected = VcapEnvironmentException.class)
    public void testReadEndpoint_empty_user_service_error() throws JSONException, VcapEnvironmentException {
        Mockito.when(vcapReaderServices.getUserProvidedServiceCredentialsByName(DashboardEndpointConfig.USER_SERVICE_NAME))
                .thenReturn(null);
        dashboardEndpointConfig.readEndpoint();
    }

    @Test(expected = VcapEnvironmentException.class)
    public void testReadEndpoint_json_error() throws JSONException, VcapEnvironmentException {
        Mockito.when(vcapReaderServices.getUserProvidedServiceCredentialsByName(DashboardEndpointConfig.ENDPOINT_SERVICE_NAME))
                .thenReturn(new JSONObject());
        dashboardEndpointConfig.readEndpoint();
    }
}