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

import com.intel.databackend.datastructures.Observation;
import org.apache.kafka.common.serialization.Serializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class KafkaJSONSerializer implements Serializer<List<Observation>> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaJSONSerializer.class);
    private final ObjectWriter jsonWriter;

    public KafkaJSONSerializer() {
        jsonWriter = new ObjectMapper().writer();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, List<Observation> data) {
        try {
            return jsonWriter.writeValueAsString(data).getBytes();
        } catch (IOException e) {
            logger.warn("Unable to serialize observation for Kafka sending.");
            return null;
        }
    }

    @Override
    public void close() {

    }
}
