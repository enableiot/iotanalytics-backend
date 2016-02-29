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
import org.apache.kafka.clients.producer.KafkaProducer;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest(KafkaConfig.class)
public class KafkaConfigTest {

    @Mock
    private ServiceConfigProvider serviceConfigProvider;

    @Mock
    private KafkaProducer<String, Observation> kafkaProducer;

    @InjectMocks
    private KafkaConfig kafkaConfig;

    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testKafkaProducer_isEnabled() throws Exception {
        Mockito.when(serviceConfigProvider.isKafkaEnabled()).thenReturn(true);
        Mockito.when(serviceConfigProvider.getKafkaUri()).thenReturn("localhost");
        PowerMockito.whenNew(KafkaProducer.class).withAnyArguments().thenReturn(kafkaProducer);

        KafkaProducer<String, Observation> kf = kafkaConfig.kafkaProducer();
        assert kf == kafkaProducer;
    }

    @Test
    public void testKafkaProducer_isDisabled() throws Exception {
        Mockito.when(serviceConfigProvider.isKafkaEnabled()).thenReturn(false);
        KafkaProducer<String, Observation> kf = kafkaConfig.kafkaProducer();
        assert kf == null;
    }
}