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
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Properties;

@Service
public class KafkaSenderService implements KafkaService {

    private KafkaProducer<String, List<Observation>> kafkaProducer;

    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    private String topic;

    @Autowired
    private ServiceConfigProvider serviceConfigProvider;

    @Autowired
    public KafkaSenderService(KafkaProducer<String, List<Observation>> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostConstruct
    public void createTopic() {
        if (kafkaProducer != null) {
            ZkClient zkClient = null;
            String brokerURI = null;
            try {
                topic = serviceConfigProvider.getKafkaTopicName();
                Integer partitions = serviceConfigProvider.getKafkaPartitionsFactor();
                Integer replicationFactor = serviceConfigProvider.getKafkaReplicationFactor();
                Integer timeoutInMs = serviceConfigProvider.getKafkaTimeoutInMs();
                brokerURI = serviceConfigProvider.getZookeeperUri();
                zkClient = new ZkClient(brokerURI, timeoutInMs, timeoutInMs, ZKStringSerializer$.MODULE$);

                if (!AdminUtils.topicExists(zkClient, topic)) {
                    logger.info("Topic: {} does not exist. Creating...", topic);
                    AdminUtils.createTopic(zkClient, topic, partitions, replicationFactor, new Properties());
                } else {
                    logger.info("Topic: {} exist and will be use for pushing messages", topic);
                }
            } catch (ZkException | VcapEnvironmentException e) {
                logger.error("error during topic creation! Topic: {}, Broker URI: {}. KafkaSenderService will be unavailable!",
                        topic, brokerURI, e);
                kafkaProducer = null;
            } finally {
                if (zkClient != null) {
                    zkClient.close();
                }
            }
        }
    }

    @Override
    public void send(List<Observation> observations) {
        if (kafkaProducer != null) {
            kafkaProducer.send(new ProducerRecord<>(topic, observations), getSendResultCallback());

        }
    }

    private Callback getSendResultCallback() {
        return (metadata, e) -> {
            if (e != null) {
                if (metadata == null) {
                    logger.error("error during sending on topic! error: ", e);
                } else {
                    logger.error("error during sending on topic {}! offset: {}, error: {}",
                            metadata.topic(), metadata.offset(), e);
                }
            } else {
                logger.debug("The offset of the sent record: {}, topic: {}", metadata.offset(), metadata.topic());
            }
        };
    }

    @PreDestroy
    protected void close() {
        if (kafkaProducer != null) {
            kafkaProducer.close();
        }
    }


}
