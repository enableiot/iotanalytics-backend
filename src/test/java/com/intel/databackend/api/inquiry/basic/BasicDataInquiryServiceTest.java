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

package com.intel.databackend.api.inquiry.basic;

import com.intel.databackend.datasources.hbase.DataDao;
import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.datastructures.requests.DataInquiryRequest;
import com.intel.databackend.datastructures.responses.DataInquiryResponse;
import com.intel.databackend.exceptions.DataInquiryException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;


public class BasicDataInquiryServiceTest {
    private DataDao dataDaoMock;
    private BasicDataInquiryService basicDataInquiryService;
    private String accountId;
    private String componentId;
    private DataInquiryRequest request;
    private Observation firstObservation, secondObservation;

    @Before
    public void SetUp() {
        dataDaoMock = Mockito.mock(DataDao.class);
        basicDataInquiryService = new BasicDataInquiryService(dataDaoMock);
        accountId = "acc1";
        componentId = "comp1";
        request = new DataInquiryRequest();
    }

    private void MockTwoObservations() throws Exception {
        Observation[] observations = new Observation[2];
        observations[0] = new Observation("1", componentId, 2L, "4");
        observations[1] = new Observation("2", componentId, 4L, "6");
        firstObservation = observations[0];
        secondObservation = observations[1];
        Mockito.when(dataDaoMock.scan(accountId, componentId, request.getStartDate(), request.getEndDate(), false, null)).thenReturn(observations);
    }

    private String getFirstObservationValue(DataInquiryResponse response) {
        return response.getComponents().get(0).getSamples().get(0).get(1);
    }

    private String getFirstObservationOn(DataInquiryResponse response) {
        return response.getComponents().get(0).getSamples().get(0).get(0);
    }

    @Test
    public void Invoke_RequestCountOnlyZeroComponents_ReturnsEmptyOkResponse() throws Exception {
        //ARRANGE
        request.setStartDate(1L);
        request.setEndDate(1L);
        request.setCountOnly(true);
        request.setComponentsWithDataType(new HashMap<>());

        basicDataInquiryService = basicDataInquiryService.withParams(accountId, request);

        //ACT
        DataInquiryResponse response = basicDataInquiryService.invoke();

        //ASSERT
        assertEquals((Long) 0L, response.getRowCount());
        assertEquals(null, response.getMaxPoints());
    }

    @Test
    public void Invoke_RequestCountOnlyTwoObservations_ReturnsTwoRowCountOkResponse() throws Exception {
        //ARRANGE
        request.setStartDate(1L);
        request.setEndDate(1L);
        request.setCountOnly(true);
        Map<String, ComponentDataType> components = new HashMap<>();
        components.put(componentId, new ComponentDataType());
        request.setComponentsWithDataType(components);

        Observation[] observations = new Observation[2];
        Mockito.when(dataDaoMock.scan(accountId, componentId, request.getStartDate(), request.getEndDate(), false, null)).thenReturn(observations);

        basicDataInquiryService = basicDataInquiryService.withParams(accountId, request);
        //ACT
        DataInquiryResponse response = basicDataInquiryService.invoke();
        //ASSERT
        assertEquals((Long) 2L, response.getRowCount());
        assertEquals(null, response.getMaxPoints());
    }

    @Test
    public void Invoke_RequestCountOnlyFalseTwoObservations_ReturnsFullResponseOkResponse() throws Exception {
        //ARRANGE
        request.setStartDate(1L);
        request.setEndDate(1L);
        request.setCountOnly(false);
        Map<String, ComponentDataType> components = new HashMap<>();
        components.put(componentId, new ComponentDataType());
        request.setComponentsWithDataType(components);
        basicDataInquiryService = basicDataInquiryService.withParams(accountId, request);

        MockTwoObservations();
        //ACT
        DataInquiryResponse response = basicDataInquiryService.invoke();

        //ASSERT
        assertEquals((Long) 2L, response.getRowCount());
        assertEquals(accountId, response.getAccountId());
        assertEquals(componentId, response.getComponents().get(0).getComponentId());
        assertEquals(firstObservation.getOn().toString(), getFirstObservationOn(response));
        assertEquals(firstObservation.getValue(), getFirstObservationValue(response));
        assertEquals(null, response.getMaxPoints());
    }

    @Test(expected = DataInquiryException.class)
    public void Invoke_NullComponentTypeFromMetadata_ThrowsDataInquiryException() throws Exception {
        //ARRANGE
        request.setStartDate(1L);
        request.setEndDate(1L);
        request.setMaxPoints(1L);

        MockTwoObservations();

        Map<String, ComponentDataType> componentData = new HashMap<String, ComponentDataType>();
        componentData.put(componentId, null);
        request.setComponentsWithDataType(componentData);
        basicDataInquiryService = basicDataInquiryService.withParams(accountId, request);

        //ACT
        basicDataInquiryService.invoke();
        //ASSERT
    }

    @Test(expected = DataInquiryException.class)
    public void Invoke_ComponentTypeNotNumberFromMetadata_ThrowsDataInquiryException() throws Exception {
        //ARRANGE
        request.setStartDate(1L);
        request.setEndDate(1L);
        request.setMaxPoints(1L);

        MockTwoObservations();

        Map<String, ComponentDataType> componentData = new HashMap<String, ComponentDataType>();
        ComponentDataType type = new ComponentDataType();
        type.setDataType("Not Number");
        componentData.put(componentId, type);
        request.setComponentsWithDataType(componentData);
        basicDataInquiryService = basicDataInquiryService.withParams(accountId, request);

        //ACT
        basicDataInquiryService.invoke();
        //ASSERT
    }

    @Test
    public void Invoke_ComponentTypeNumberFromMetadata_ReturnAverageOf10Is5() throws Exception {
        //ARRANGE
        request.setStartDate(1L);
        request.setEndDate(5L);
        request.setMaxPoints(1L);

        MockTwoObservations();

        Map<String, ComponentDataType> componentData = new HashMap<String, ComponentDataType>();
        ComponentDataType type = new ComponentDataType();
        type.setDataType(ComponentDataType.NUMBER);
        componentData.put(componentId, type);
        request.setComponentsWithDataType(componentData);

        basicDataInquiryService = basicDataInquiryService.withParams(accountId, request);
        //ACT
        DataInquiryResponse response = basicDataInquiryService.invoke();

        //ASSERT
        assertEquals((Long) 2L, response.getRowCount());
        assertEquals(accountId, response.getAccountId());
        assertEquals(componentId, response.getComponents().get(0).getComponentId());

        String expectedTimeAvg = String.valueOf((firstObservation.getOn() + secondObservation.getOn()) / 2);
        String expectedValueAvg = String.valueOf((Double.parseDouble(firstObservation.getValue()) + Double.parseDouble(secondObservation.getValue())) / 2L);
        assertEquals(expectedTimeAvg, getFirstObservationOn(response));
        assertEquals(expectedValueAvg, getFirstObservationValue(response));
        assertEquals(null, response.getMaxPoints());
    }
}
