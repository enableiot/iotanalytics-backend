/**
 * Copyright (c) 2015 Intel Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KafkaSenderService.class, AdminUtils.class})
public class KafkaSenderServiceTest {

    public static final String TOPIC = "testTopic";

    @Mock
    ZkClient zkClient;

    @Mock
    private ServiceConfigProvider serviceConfigProvider;

    @Mock
    private KafkaProducer<String, Observation> kafkaProducer;

    @InjectMocks
    private KafkaSenderService kafkaSenderService;

    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.whenNew(ZkClient.class).withAnyArguments().thenReturn(zkClient);
        Mockito.doNothing().when(zkClient).close();

        PowerMockito.mockStatic(AdminUtils.class);
        Mockito.when(AdminUtils.topicExists(zkClient, TOPIC)).thenReturn(true);

        Mockito.when(serviceConfigProvider.getKafkaTopicName()).thenReturn(TOPIC);
        Mockito.when(serviceConfigProvider.getKafkaPartitionsFactor()).thenReturn(1);
        Mockito.when(serviceConfigProvider.getKafkaReplicationFactor()).thenReturn(1);
        Mockito.when(serviceConfigProvider.getKafkaTimeoutInMs()).thenReturn(10);
        Mockito.when(serviceConfigProvider.getZookeeperUri()).thenReturn("localhost");
    }

    @After
    public void after() {
        PowerMockito.verifyStatic();
    }

    @Test
    public void testCreateTopic_topic_exist() throws VcapEnvironmentException {
        kafkaSenderService.createTopic();
        kafkaSenderService.finalize();
        Mockito.verify(kafkaProducer).close();
        Mockito.verifyNoMoreInteractions(kafkaProducer);
    }

    @Test
    public void testCreateTopic_topic_not_exist() throws VcapEnvironmentException {
        Mockito.when(AdminUtils.topicExists(zkClient, TOPIC)).thenReturn(false);
        kafkaSenderService.createTopic();
        kafkaSenderService.finalize();
        Mockito.verify(kafkaProducer).close();
        Mockito.verifyNoMoreInteractions(kafkaProducer);

    }

    @Test
    public void testCreateTopic_error_handling() throws Exception {
        Mockito.when(AdminUtils.topicExists(zkClient, TOPIC)).thenThrow(new NullPointerException());
        kafkaSenderService.createTopic();
        kafkaSenderService.finalize();
        Mockito.verifyNoMoreInteractions(kafkaProducer);
    }

    @Test
    public void testSend() throws Exception {
        kafkaSenderService.createTopic();
        kafkaSenderService.send(Arrays.asList(new Observation()));
        Mockito.verify(kafkaProducer).send(Mockito.anyObject(), Mockito.anyObject());
    }
}