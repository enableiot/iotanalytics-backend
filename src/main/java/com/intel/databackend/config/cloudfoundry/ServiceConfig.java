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

import com.intel.databackend.config.ServiceConfigProvider;
import com.intel.databackend.config.cloudfoundry.utils.VcapReader;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ServiceConfig implements ServiceConfigProvider {

    public static final String KAFKA_SERVICE_NAME = "kafka";
    public static final String KAFKA_SERVICE_URI = "uri";
    public static final String KAFKA_UPS_NAME = "kafka-ups";
    public static final String KAFKA_UPS_TOPIC = "topic";
    public static final String KAFKA_UPS_ENABLED = "enabled";
    public static final String KAFKA_UPS_PARTITIONS = "partitions";
    public static final String KAFKA_UPS_REPLICATION = "replication";
    public static final String KAFKA_UPS_TIMEOUT_MS = "timeout_ms";

    public static final String ZOOKEEPER_BROKER_NAME = "zookeeper";
    public static final String ZOOKEEPER_BROKER_URI = "zk.cluster";
    public static final String ZOOKEEPER_BROKER_PLAN = "plan";

    public static final String LOCAL_PLAN = "local";

    @Autowired
    private VcapReader vcapReaderServices;
    private JSONObject kafkaCredentials;
    private JSONObject kafkaSettings;
    private JSONObject zookeeperService;
    private JSONObject zookeeperCredentials;

    public ServiceConfig() {
    }

    @PostConstruct
    public void init() {
        kafkaCredentials = vcapReaderServices.getVcapServiceCredentialsByType(KAFKA_SERVICE_NAME);
        kafkaSettings = vcapReaderServices.getUserProvidedServiceCredentialsByName(KAFKA_UPS_NAME);
        zookeeperService = vcapReaderServices.getVcapServiceByType(ZOOKEEPER_BROKER_NAME);
        zookeeperCredentials = vcapReaderServices.getVcapServiceCredentialsByType(ZOOKEEPER_BROKER_NAME);
    }

    @Override
    public String getKafkaUri() throws VcapEnvironmentException {
        return getFieldValueFromJson(kafkaCredentials, KAFKA_SERVICE_NAME, KAFKA_SERVICE_URI, String.class);
    }

    @Override
    public String getZookeeperUri() throws VcapEnvironmentException {
        /*
          This is dirty workaround for dev's local machines
          On local kafka instance we cannot use '/kafka' postfix in URI
         */
        String plan = getFieldValueFromJson(zookeeperService, ZOOKEEPER_BROKER_NAME,
                ZOOKEEPER_BROKER_PLAN, String.class);
        if (StringUtils.isNotEmpty(plan) && plan.equals(LOCAL_PLAN)) {
            return getFieldValueFromJson(zookeeperCredentials, ZOOKEEPER_BROKER_NAME,
                    ZOOKEEPER_BROKER_URI, String.class);
        } else {
            return getFieldValueFromJson(zookeeperCredentials, ZOOKEEPER_BROKER_NAME,
                    ZOOKEEPER_BROKER_URI, String.class) + "/kafka";
        }
    }

    @Override
    public Boolean isKafkaEnabled() throws VcapEnvironmentException {
        return getFieldValueFromJson(kafkaSettings, KAFKA_UPS_NAME, KAFKA_UPS_ENABLED, Boolean.class);
    }

    @Override
    public String getKafkaTopicName() throws VcapEnvironmentException {
        return getFieldValueFromJson(kafkaSettings, KAFKA_UPS_NAME, KAFKA_UPS_TOPIC, String.class);
    }

    @Override
    public Integer getKafkaPartitionsFactor() throws VcapEnvironmentException {
        return getFieldValueFromJson(kafkaSettings, KAFKA_UPS_NAME, KAFKA_UPS_PARTITIONS, Integer.class);
    }

    @Override
    public Integer getKafkaReplicationFactor() throws VcapEnvironmentException {
        return getFieldValueFromJson(kafkaSettings, KAFKA_UPS_NAME, KAFKA_UPS_REPLICATION, Integer.class);
    }

    @Override
    public Integer getKafkaTimeoutInMs() throws VcapEnvironmentException {
        return getFieldValueFromJson(kafkaSettings, KAFKA_UPS_NAME, KAFKA_UPS_TIMEOUT_MS, Integer.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T getFieldValueFromJson(JSONObject jsonObj, String type, String field, Class<T> tClass)
            throws VcapEnvironmentException {
        if (jsonObj != null) {
            try {
                if (tClass.equals(String.class)) {
                    return (T) jsonObj.getString(field);
                } else if (tClass.equals(Integer.class)) {
                    return (T) (Integer) jsonObj.getInt(field);
                } else if (tClass.equals(Boolean.class)) {
                    return (T) (Boolean) jsonObj.getBoolean(field);
                } else {
                    return null;
                }
            } catch (JSONException e) {
                throw new VcapEnvironmentException("Unable to parse json config from VCAP env - " + type, e);
            }
        } else {
            throw new VcapEnvironmentException("Unable to find json config in VCAP env - " + type);
        }
    }
}
