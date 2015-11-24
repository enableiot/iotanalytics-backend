/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.databackend.api.inquiry.samples;

import com.intel.databackend.api.inquiry.DataRetrieveParams;
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.datastructures.requests.DataInquiryRequest;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


public class SampleAggregationDataRetrieverTest {
    
    @Test
    public void get_maxPointsSmallerThanAllObservations_returnsAveragedObservations() {
        //ARRANGE
        Observation[] observations = {new Observation("aid", "cid", 10l, "200.0"), new Observation("aid", "cid", 11l, "201.0"),
            new Observation("aid", "cid", 12l, "202.0"), new Observation("aid", "cid", 13l, "203.0")};
        DataInquiryRequest dataInquiryRequest = new DataInquiryRequest();
        dataInquiryRequest.setStartDate(10l);
        dataInquiryRequest.setEndDate(14l);
        dataInquiryRequest.setMaxPoints(2l);
        DataRetrieveParams params = new DataRetrieveParams(dataInquiryRequest, null);
        SampleAggregationDataRetriever instance = new SampleAggregationDataRetriever(params);

        //ACT
        List<List<String>> result = instance.get(observations, null, null);

        //ASSERT
        //values are averaged in each bucket
        assertThat(result, is(Arrays.asList(Arrays.asList("10", "200.5"), Arrays.asList("12", "202.5"))));
    }
    
    @Test
    public void get_maxPointsLargerThanTimespan_samplesEveryMillisecond() {
        //ARRANGE
        Observation[] observations = {new Observation("aid", "cid", 10l, "200.0"), new Observation("aid", "cid", 11l, "201.0")};
        DataInquiryRequest dataInquiryRequest = new DataInquiryRequest();
        dataInquiryRequest.setStartDate(10l);
        dataInquiryRequest.setEndDate(12l);
        dataInquiryRequest.setMaxPoints(1000l);
        DataRetrieveParams params = new DataRetrieveParams(dataInquiryRequest, null);
        SampleAggregationDataRetriever instance = new SampleAggregationDataRetriever(params);

        //ACT
        List<List<String>> result = instance.get(observations, null, null);

        //ASSERT
        assertThat(result, is(Arrays.asList(Arrays.asList("10", "200.0"), Arrays.asList("11", "201.0"))));
    }
}
