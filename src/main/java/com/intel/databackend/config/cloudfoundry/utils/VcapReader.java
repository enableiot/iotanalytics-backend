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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VcapReader {
    private static final Logger logger = LoggerFactory.getLogger(VcapReader.class);

    public static final String VCAP_SERVICES = "VCAP_SERVICES";
    public static final String USER_PROVIDED = "user-provided";
    public static final String CREDENTIALS = "credentials";
    public static final String NAME = "name";


    private final String vcapServices;

    public VcapReader() {
        vcapServices = System.getenv(VCAP_SERVICES);
    }

    public JSONObject getVcapServiceCredentialsByType(String type) {
        try {
            if (vcapServices != null) {
                return new JSONObject(vcapServices).getJSONArray(type).getJSONObject(0).getJSONObject(CREDENTIALS);
            }
        } catch (JSONException e) {
            logger.warn("Cannot find service of type - {}", type);
        }
        return null;
    }

    public JSONObject getUserProvidedServiceCredentialsByName(String name) {
        try {
            if (vcapServices != null) {
                JSONArray upses = new JSONObject(vcapServices).getJSONArray(USER_PROVIDED);

                for (int i = 0; i < upses.length(); i++) {
                    JSONObject ups = upses.getJSONObject(i);
                    if (ups.getString(NAME).equals(name)) {
                        return ups.getJSONObject(CREDENTIALS);
                    }
                }
            }
        } catch (JSONException e) {
            logger.warn("Cannot find user provided service - {} in VCAP_SERVICES", name);
            logger.warn(e.getMessage());
        }
        return null;
    }

    public JSONObject getVcapServiceByType(String type) {
        try {
            if (vcapServices != null) {
                return new JSONObject(vcapServices).getJSONArray(type).getJSONObject(0);
            }
        } catch (JSONException e) {
            logger.warn("Cannot find service of type - {} in VCAP_SERVICES", type);
        }
        return null;
    }
}
