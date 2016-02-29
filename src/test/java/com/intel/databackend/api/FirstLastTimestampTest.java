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

package com.intel.databackend.api;

import com.intel.databackend.datasources.hbase.DataDao;
import com.intel.databackend.datastructures.ComponentMeasurementTimestamps;
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.datastructures.requests.FirstLastTimestampRequest;
import com.intel.databackend.datastructures.responses.FirstLastTimestampResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;


public class FirstLastTimestampTest {

    private DataDao dataDaoMock;
    private Service<FirstLastTimestampRequest, FirstLastTimestampResponse> firstLastTimestamp;
    private String accountId;

    @Before
    public void SetUp() {
        dataDaoMock = Mockito.mock(DataDao.class);
        firstLastTimestamp = new FirstLastTimestampService(dataDaoMock);
        accountId = "acc1";
    }

    @Test
    public void Invoke_EmptyComponentsListInDb_ReturnsOkResponseWithEmptyList() throws Exception {
        //ARRANGE
        List<String> emptyList = new ArrayList<>();

        FirstLastTimestampRequest request = new FirstLastTimestampRequest();
        request.setComponents(new ArrayList<String>());
        request.getComponents().add("test");

        firstLastTimestamp.withParams("acc1", request);

        //ACT
        FirstLastTimestampResponse res = firstLastTimestamp.invoke();

        //ASSERT

        assertEquals("inquiryComponentFirstAndLastResponse", res.getMsgType());
        assertEquals(0, res.getComponentsFirstLast().size());
    }

    @Test
    public void Invoke_EmptyComponentsListInRequest_ReturnsOkResponseWithEmptyList() throws Exception {
        //ARRANGE
        List<String> componentsList = new ArrayList<>();
        componentsList.add("test");

        FirstLastTimestampRequest request = new FirstLastTimestampRequest();
        request.setComponents(new ArrayList<String>());

        firstLastTimestamp.withParams("acc1", request);

        //ACT
        FirstLastTimestampResponse res = firstLastTimestamp.invoke();

        //ASSERT

        assertEquals("inquiryComponentFirstAndLastResponse", res.getMsgType());
        assertEquals(0, res.getComponentsFirstLast().size());
    }

    @Test
    public void Invoke_EmptyObservation_ReturnsOkResponseWithEmptyList() throws Exception {
        //ARRANGE
        List<String> componentsList = new ArrayList<>();
        componentsList.add("test");

        Mockito.when(dataDaoMock.scan(accountId, "test", 0L, Long.MAX_VALUE, false, null, true, 1)).thenReturn(new Observation[0]);

        FirstLastTimestampRequest request = new FirstLastTimestampRequest();
        request.setComponents(new ArrayList<String>());
        request.getComponents().add("test");

        firstLastTimestamp.withParams("acc1", request);

        //ACT
        FirstLastTimestampResponse res = firstLastTimestamp.invoke();

        //ASSERT
        assertEquals("inquiryComponentFirstAndLastResponse", res.getMsgType());
        assertEquals(0, res.getComponentsFirstLast().size());
    }

    @Test
    public void Invoke_Observations_ReturnsOkResponseWithEmptyList() throws Exception {
        //ARRANGE
        List<String> componentsList = new ArrayList<>();
        componentsList.add("test");

        Observation[] observations = new Observation[]{new Observation("aid", "cid", 1, "val")};

        Mockito.when(dataDaoMock.scan(accountId, "test", 0L, Long.MAX_VALUE, false, null, true, 1)).thenReturn(observations);
        Mockito.when(dataDaoMock.scan(accountId, "test", 0L, Long.MAX_VALUE, false, null, false, 1)).thenReturn(observations);

        FirstLastTimestampRequest request = new FirstLastTimestampRequest();
        request.setComponents(new ArrayList<String>());
        request.getComponents().add("test");

        firstLastTimestamp.withParams("acc1", request);

        FirstLastTimestampResponse expectedResponse = new FirstLastTimestampResponse();
        expectedResponse.setComponentsFirstLast(new ArrayList<ComponentMeasurementTimestamps>());
        ComponentMeasurementTimestamps cmts = new ComponentMeasurementTimestamps();
        cmts.setComponentId("test");
        cmts.setFirstPointTS(1L);
        cmts.setLastPointTS(1L);
        expectedResponse.getComponentsFirstLast().add(cmts);


        //ACT
        FirstLastTimestampResponse res = firstLastTimestamp.invoke();

        //ASSERT
        assertEquals("inquiryComponentFirstAndLastResponse", res.getMsgType());
        assertEquals(1, res.getComponentsFirstLast().size());
        assertEquals(expectedResponse.getComponentsFirstLast().get(0).getFirstPointTS(), res.getComponentsFirstLast().get(0).getFirstPointTS());
        assertEquals(expectedResponse.getComponentsFirstLast().get(0).getLastPointTS(), res.getComponentsFirstLast().get(0).getLastPointTS());
    }
}
