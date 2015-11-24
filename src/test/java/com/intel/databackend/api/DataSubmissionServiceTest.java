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
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.datastructures.requests.DataSubmissionRequest;
import com.intel.databackend.datastructures.responses.DataSubmissionResponse;
import com.intel.databackend.exceptions.MissingDataSubmissionArgumentException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.testng.Assert.assertEquals;


public class DataSubmissionServiceTest {
    private DataDao dataDaoMock;
    private Service<DataSubmissionRequest, DataSubmissionResponse> dataSubmissionService;
    private String accountId;
    private DataSubmissionRequest request;


    @Before
    public void SetUp(){
        dataDaoMock = Mockito.mock(DataDao.class);
        accountId = "acc1";
        request = new DataSubmissionRequest();
        dataSubmissionService =  new DataSubmissionService(dataDaoMock);
    }

    @Test(expected=MissingDataSubmissionArgumentException.class)
    public void Invoke_RequestDataNull_ReturnsBadRequest() throws Exception{
        //ARRANGE
        dataSubmissionService.withParams(accountId, request);

        //ACT
        dataSubmissionService.invoke();

    }

    @Test
    public void Invoke_RequestDataProvided_SetsAccountIdInData() throws Exception{
        //ARRANGE
        Observation observation = new Observation("oldAid", "cid", 1L, "value");
        List<Observation> data = new ArrayList<>();
        data.add(observation);
        request.setData(data);
        Mockito.when(dataDaoMock.put(any(Observation[].class))).thenReturn(true);

        dataSubmissionService.withParams(accountId, request);

        //ACT
        dataSubmissionService.invoke();

        //ASSERT
        Mockito.verify(dataDaoMock, Mockito.times(1)).put(any(Observation[].class));
        assertEquals(observation.getAid(), accountId);
    }

}
