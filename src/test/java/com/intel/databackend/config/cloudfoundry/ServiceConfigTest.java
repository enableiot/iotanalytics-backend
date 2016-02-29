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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceConfigTest {

    @Mock
    private VcapReader vcapReaderServices;

    @InjectMocks
    private ServiceConfig serviceConfig;

    @Test
    public void Invoke_getUserProvidedServiceCredentialsByName() throws VcapEnvironmentException, JSONException {
        String topic = "metrics";

        Mockito.when(vcapReaderServices.getUserProvidedServiceCredentialsByName(ServiceConfig.KAFKA_UPS_NAME))
                .thenReturn(new JSONObject("{" + ServiceConfig.KAFKA_UPS_TOPIC + ": " + topic + "}"));
        serviceConfig.init();
        Assert.assertEquals(serviceConfig.getKafkaTopicName(), topic);
    }

    @Test
    public void Invoke_getKafkaUri() throws VcapEnvironmentException, JSONException {
        String uri = "localhost";

        Mockito.when(vcapReaderServices.getVcapServiceCredentialsByType(ServiceConfig.KAFKA_SERVICE_NAME))
                .thenReturn(new JSONObject("{" + ServiceConfig.KAFKA_SERVICE_URI + ": " + uri + "}"));
        serviceConfig.init();
        Assert.assertEquals(serviceConfig.getKafkaUri(), uri);
    }

    @Test
    public void Invoke_getZookeeperUri() throws VcapEnvironmentException, JSONException {
        String uriLocal = "localhost";

        Mockito.when(vcapReaderServices.getVcapServiceByType(ServiceConfig.ZOOKEEPER_BROKER_NAME))
                .thenReturn(new JSONObject("{" + ServiceConfig.ZOOKEEPER_BROKER_PLAN + ": " + ServiceConfig.LOCAL_PLAN + "}"));
        Mockito.when(vcapReaderServices.getVcapServiceCredentialsByType(ServiceConfig.ZOOKEEPER_BROKER_NAME))
                .thenReturn(new JSONObject("{" + ServiceConfig.ZOOKEEPER_BROKER_URI + ": " + uriLocal + "}"));
        serviceConfig.init();
        Assert.assertEquals(serviceConfig.getZookeeperUri(), uriLocal);

        Mockito.when(vcapReaderServices.getVcapServiceByType(ServiceConfig.ZOOKEEPER_BROKER_NAME))
                .thenReturn(new JSONObject("{" + ServiceConfig.ZOOKEEPER_BROKER_PLAN + ": " + "prod" + "}"));
        serviceConfig.init();
        Assert.assertEquals(serviceConfig.getZookeeperUri(), uriLocal + "/kafka");
    }

    @Test(expected = VcapEnvironmentException.class)
    public void Throws_error_when_response_is_empty() throws VcapEnvironmentException, JSONException {
        Mockito.when(vcapReaderServices.getVcapServiceCredentialsByType(ServiceConfig.KAFKA_SERVICE_NAME))
                .thenReturn(null);
        serviceConfig.init();
        serviceConfig.getKafkaUri();
    }

    @Test(expected = VcapEnvironmentException.class)
    public void Throws_error_when_response_not_contain_key() throws VcapEnvironmentException, JSONException {
        Mockito.when(vcapReaderServices.getVcapServiceCredentialsByType(ServiceConfig.KAFKA_SERVICE_NAME))
                .thenReturn(new JSONObject("{" + ServiceConfig.KAFKA_UPS_TOPIC + ": test}"));
        serviceConfig.init();
        serviceConfig.getKafkaUri();
    }
}
