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

package com.intel.databackend.config.cloudfoundry.utils;

import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VcapReader.class)
public class VcapReaderTest {

    private VcapReader vcapReader;

    private final static String kafkaCredential = "{\"uri\": \"europe:9092\"}";
    private final static String kafka = "{\"credentials\":" + kafkaCredential + ",\"name\":\"mykafka\"}";
    private final static String kerberosCredential = "{\"kuser\": \"user\"}";

    private final static String vcap = "{\n" +
            "\"kafka\": [" + kafka + "],\n" +
            "\"user-provided\": [{\n" +
            "    \"credentials\":" + kerberosCredential +
            "  , \"name\": \"kerberos-service\"\n" +
            "}]" +
            "}";

    @Before
    public void init() {
        PowerMockito.mockStatic(System.class);
        Mockito.when(System.getenv(VcapReader.VCAP_SERVICES)).thenReturn(vcap);
        vcapReader = new VcapReader();
    }

    @After
    public void after() {
        PowerMockito.verifyStatic();
        System.getenv(VcapReader.VCAP_SERVICES);
    }

    @Test
    public void testGetVcapServiceCredentialsByType() throws Exception {
        JSONObject result = vcapReader.getVcapServiceCredentialsByType("kafka");
        assert result != null;
        assert result.toString().equals(new JSONObject(kafkaCredential).toString());

        result = vcapReader.getVcapServiceCredentialsByType("kafka-fake");
        assert result == null;
    }

    @Test
    public void testGetUserProvidedServiceCredentialsByName() throws Exception {
        JSONObject result = vcapReader.getUserProvidedServiceCredentialsByName("kerberos-service");
        assert result != null;
        assert result.toString().equals(new JSONObject(kerberosCredential).toString());

        result = vcapReader.getUserProvidedServiceCredentialsByName("kerberos-fake");
        assert result == null;
    }

    @Test
    public void testGetVcapServiceByType() throws Exception {
        JSONObject result = vcapReader.getVcapServiceByType("kafka");
        assert result != null;
        assert result.toString().equals(new JSONObject(kafka).toString());

        result = vcapReader.getVcapServiceByType("kafka-fake");
        assert result == null;
    }
}