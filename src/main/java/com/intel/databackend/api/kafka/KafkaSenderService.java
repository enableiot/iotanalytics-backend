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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;

@Service
public class KafkaSenderService implements KafkaService {

    private KafkaProducer<String, Observation> kafkaProducer;

    @Value("${kafka.topic}")
    private String topic;

    @Autowired
    public KafkaSenderService(KafkaProducer<String, Observation> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void send(List<Observation> observations) {
        if (kafkaProducer != null) {
            observations.stream()
                    .forEach(observation -> kafkaProducer.send(new ProducerRecord<>(topic, observation)));
        }
    }

    @PreDestroy
    protected void finalize() {
        if (kafkaProducer != null) {
            kafkaProducer.close();
        }
    }


}
