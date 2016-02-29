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

package com.intel.databackend.api.inquiry.samples;

import com.intel.databackend.api.inquiry.DataRetrieveParams;
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.datastructures.requests.DataInquiryRequest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class SampleAggregationDataRetrieverTest {

    @Test
    public void get_maxPointsSmallerThanAllObservations_returnsAveragedObservations() {
        //ARRANGE
        Observation[] observations = {new Observation("aid", "cid", 10L, "200.0"), new Observation("aid", "cid", 11L, "201.0"),
                new Observation("aid", "cid", 12L, "202.0"), new Observation("aid", "cid", 13L, "203.0")};
        DataInquiryRequest dataInquiryRequest = new DataInquiryRequest();
        dataInquiryRequest.setStartDate(10L);
        dataInquiryRequest.setEndDate(14L);
        dataInquiryRequest.setMaxPoints(2L);
        DataRetrieveParams params = new DataRetrieveParams(dataInquiryRequest, null);
        SampleAggregationDataRetriever instance = new SampleAggregationDataRetriever(params);

        //ACT
        List<List<String>> result = instance.get(observations, null, null);

        //ASSERT
        //values are averaged in each bucket
        assertEquals(result, Arrays.asList(Arrays.asList("10", "200.5"), Arrays.asList("12", "202.5")));
    }

    @Test
    public void get_maxPointsLargerThanTimespan_samplesEveryMillisecond() {
        //ARRANGE
        Observation[] observations = {new Observation("aid", "cid", 10L, "200.0"), new Observation("aid", "cid", 11L, "201.0")};
        DataInquiryRequest dataInquiryRequest = new DataInquiryRequest();
        dataInquiryRequest.setStartDate(10L);
        dataInquiryRequest.setEndDate(12L);
        dataInquiryRequest.setMaxPoints(1000L);
        DataRetrieveParams params = new DataRetrieveParams(dataInquiryRequest, null);
        SampleAggregationDataRetriever instance = new SampleAggregationDataRetriever(params);

        //ACT
        List<List<String>> result = instance.get(observations, null, null);

        //ASSERT
        assertEquals(result, Arrays.asList(Arrays.asList("10", "200.0"), Arrays.asList("11", "201.0")));
    }

    @Test
    public void get_maxPointsSmallerThanAllObservations_manyObservations_returnsAveraged() {
        Observation[] observations = new Observation[1000];
        for (int i = 0; i < observations.length; i++) {
            observations[i] = new Observation("aid", "cid", i, String.valueOf(i * 10.0));
        }

        DataInquiryRequest dataInquiryRequest = new DataInquiryRequest();
        dataInquiryRequest.setStartDate(0l);
        dataInquiryRequest.setEndDate(999l);
        dataInquiryRequest.setMaxPoints(300l);
        DataRetrieveParams params = new DataRetrieveParams(dataInquiryRequest, null);
        SampleAggregationDataRetriever instance = new SampleAggregationDataRetriever(params);

        //ACT
        List<List<String>> result = instance.get(observations, null, null);

        //ASSERT
        //bucket size is floor(999ms/300) = 3ms
        //number of buckets is ceil(1000/3)=334. We have actualy 1000 data points from 1000 different milliseconds.
        assertEquals(334, result.size());
        for (int i = 0; i < result.size() - 1; i++) {
            //each bucket gets data points from 3ms and the test data has exactly 3 data points in every 3ms.
            //bucket i groups orignal data points with time at 3*i, 3*i+1, 3*i+2 so the average is (9*i+3)/3.
            //The same applies to the values which are 10*time for each data point.
            assertEquals(String.valueOf(3 * i + 1), result.get(i).get(0));
            assertEquals(String.valueOf(30.0 * i + 10), result.get(i).get(1));
        }
        //number of data points in this test is not divisible by 3 and the last value 
        //is the only one in its bucket and is not averaged with anything.
        assertEquals(String.valueOf(999), result.get(result.size() - 1).get(0));
        assertEquals(String.valueOf(9990.0), result.get(result.size() - 1).get(1));
    }
}
