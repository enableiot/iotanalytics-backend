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

import com.intel.databackend.config.ZookeeperCredentialsProvider;
import com.intel.databackend.config.cloudfoundry.utils.VcapReader;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ZookeeperConfig implements ZookeeperCredentialsProvider {

    private static final String SERVICE_NAME = "zookeeper-wssb";
    private static final String URI = "uri";

    private VcapReader vcapReaderServices = null;


    public ZookeeperConfig() {
        vcapReaderServices = new VcapReader();
    }

    public String[] getZookeeperHosts() throws VcapEnvironmentException {
        JSONObject zookeeperConf = vcapReaderServices.getVcapServiceCredentialsByType(SERVICE_NAME);
        if (zookeeperConf != null) {
            try {
                return zookeeperConf.getString(URI).split(",");
            } catch (JSONException e) {
                throw new VcapEnvironmentException("Unable to parse Zookeeper config from VCAP env - " + SERVICE_NAME, e);
            }
        } else {
            throw new VcapEnvironmentException("Unable to find Zookeeper config in VCAP env - " + SERVICE_NAME);
        }
    }
}
