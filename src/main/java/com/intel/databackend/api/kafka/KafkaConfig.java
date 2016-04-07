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
package com.intel.databackend.api.kafka;

import com.intel.databackend.config.ServiceConfigProvider;
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration
public class KafkaConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    @Autowired
    private ServiceConfigProvider serviceConfigProvider;

    private final Serializer<String> keySerializer = new StringSerializer();

    private final Serializer<List<Observation>> valueSerializer = new KafkaJSONSerializer();

    @Bean
    public KafkaProducer<String, List<Observation>> kafkaProducer() throws VcapEnvironmentException {
        try {
            if (serviceConfigProvider.isKafkaEnabled()) {
                Map<String, Object> producerConfig = new HashMap<>();
                producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serviceConfigProvider.getKafkaUri());
                return new KafkaProducer<>(producerConfig, keySerializer, valueSerializer);
            }
        } catch (VcapEnvironmentException e) {
            logger.error("Kafka configuration is not available.", e);
        }
        logger.info("Kafka is not available. No data will be ingested into Kafka broker.");
        return null;


    }
}