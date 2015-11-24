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

package com.intel.databackend.api.inquiry.advanced;

import com.intel.databackend.datasources.dashboard.components.ComponentsDao;
import com.intel.databackend.datasources.dashboard.devices.DeviceDao;
import com.intel.databackend.datasources.dashboard.utils.QueryParam;
import com.intel.databackend.datasources.hbase.DataDao;
import com.intel.databackend.datastructures.AdvancedComponent;
import com.intel.databackend.datastructures.DeviceData;
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.datastructures.requests.AdvDataInquiryRequest;
import com.intel.databackend.datastructures.responses.AdvDataInquiryResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.testng.Assert.assertEquals;


public class AdvancedDataInquiryServiceTest {
    private DataDao dataDaoMock;
    private ComponentsDao componentsDaoMock;
    private DeviceDao deviceDaoMock;

    private AdvancedDataInquiryService advancedDataInquiryService;
    private String accountId;
    private AdvDataInquiryRequest request;




    @Before
    public void SetUp(){
        dataDaoMock = Mockito.mock(DataDao.class);
        componentsDaoMock = Mockito.mock(ComponentsDao.class);
        deviceDaoMock = Mockito.mock(DeviceDao.class);

        accountId = java.util.UUID.randomUUID().toString();
        request = new AdvDataInquiryRequest();
        request.setStartTimestamp(1L);
        request.setEndTimestamp(1L);
        advancedDataInquiryService =  new AdvancedDataInquiryService(dataDaoMock, componentsDaoMock, deviceDaoMock);
    }

    @Test
    public void Invoke_ComponentIdsNull_ReturnsEmptyAdvInquiryResponse() throws Exception{
        //ARRANGE
        advancedDataInquiryService = advancedDataInquiryService.withParams(accountId, request);

        //ACT
        AdvDataInquiryResponse response = advancedDataInquiryService.invoke();

        //ASSERT
        assertEquals(accountId, response.getAccountId());
        assertEquals(0, response.getData().size());
        assertEquals(null, response.getRowCount());
        Mockito.verify(dataDaoMock, Mockito.times(0)).scan(any(String.class), any(String.class), any(Long.class), any(Long.class), any(Boolean.class), any(String[].class));
        Mockito.verify(deviceDaoMock, Mockito.times(0)).getDevicesFromAccount(any(String.class), Matchers.<List<String>>any());
        Mockito.verify(componentsDaoMock, Mockito.times(1)).getComponentsByCustomParams(any(String.class), Matchers.<List<QueryParam>>any());
    }

    @Test
    public void Invoke_RequestDataProvidedCountOnlyFalse_ReturnsData() throws Exception{
        //ARRANGE
        Observation observation = new Observation(accountId, "cid", 1L, "value");
        Observation[] observations = new Observation[1];
        observations[0] = observation;
        String[] stringAttributes = new String[0];
        List<String> data = new ArrayList<>();
        data.add(observation.getCid());
        request.setComponentIds(data);


        Mockito.when(componentsDaoMock.getComponentsByCustomParams(any(String.class), anyListOf(QueryParam.class))).thenReturn(data);
        Mockito.when(dataDaoMock.put(any(Observation[].class))).thenReturn(true);
        Mockito.when(dataDaoMock.scan(any(String.class),
                any(String.class),
                any(Long.class),
                any(Long.class),
                any(Boolean.class),
                any(String[].class))).thenReturn(observations);

        Mockito.when(dataDaoMock.scanForAttributeNames(any(String.class),
                any(String.class),
                any(Long.class),
                any(Long.class))).thenReturn(stringAttributes);

        List<DeviceData> devices = new ArrayList<>();
        DeviceData devData = new DeviceData();

        AdvancedComponent advComponent = new AdvancedComponent();
        advComponent.setComponentId("cid");
        List<AdvancedComponent> components = new ArrayList<>();
        components.add(advComponent);
        devData.setComponents(components);
        devices.add(devData);
        Mockito.when(deviceDaoMock.getDevicesFromAccount(accountId, data)).thenReturn(devices);

        advancedDataInquiryService = advancedDataInquiryService.withParams(accountId, request);

        //ACT
        AdvDataInquiryResponse response = advancedDataInquiryService.invoke();

        //ASSERT
        assertEquals(accountId, response.getAccountId());
        assertEquals((Long) 1L, response.getRowCount());
        assertEquals(1, response.getData().size());
        assertEquals(1, response.getData().get(0).getComponents().size());
        assertEquals(advComponent.getComponentId(), response.getData().get(0).getComponents().get(0).getComponentId());
    }

    @Test
    public void Invoke_RequestDataProvidedCountOnly_SetsAccountIdInData() throws Exception{
        //ARRANGE
        Observation observation = new Observation(accountId, "cid", 1L, "value");
        Observation[] observations = new Observation[1];
        observations[0] = observation;
        String[] stringAttributes = new String[0];
        List<String> data = new ArrayList<>();
        data.add(observation.getCid());
        request.setComponentIds(data);
        request.setCountOnly(true);

        Mockito.when(componentsDaoMock.getComponentsByCustomParams(any(String.class), Matchers.<List<QueryParam>>any())).thenReturn(data);
        Mockito.when(dataDaoMock.put(any(Observation[].class))).thenReturn(true);
        Mockito.when(dataDaoMock.scan(any(String.class),
                any(String.class),
                any(Long.class),
                any(Long.class),
                any(Boolean.class),
                any(String[].class))).thenReturn(observations);

        Mockito.when(dataDaoMock.scanForAttributeNames(any(String.class),
                any(String.class),
                any(Long.class),
                any(Long.class))).thenReturn(stringAttributes);

        advancedDataInquiryService = advancedDataInquiryService.withParams(accountId, request);

        //ACT
        AdvDataInquiryResponse response = advancedDataInquiryService.invoke();

        //ASSERT
        assertEquals(accountId, response.getAccountId());
        assertEquals((Long)1L, response.getRowCount());
    }
}