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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SampleAggregationDataRetriever implements SampleDataRetriever {

    private static final Logger logger = LoggerFactory.getLogger(SampleAggregationDataRetriever.class);

    private final long startDate;
    private final long bucketTimePeriod;

    public SampleAggregationDataRetriever(DataRetrieveParams dataRetrieveParams) {
        this.startDate = dataRetrieveParams.getStartDate();
        this.bucketTimePeriod = calculateBucketTimePeriod(dataRetrieveParams);

        logger.debug("Max points = {}", dataRetrieveParams.getMaxPoints());
        logger.debug("bucketTimePeriod = {}", bucketTimePeriod);
    }

    @Override
    public List<List<String>> get(Observation[] observations, Long first, Long last) {
        //group by timestamp bucket and count averages in each bucket.
        //Stream based solution could be easier to parallelize (and read).

        List<List<String>> sampleObservationList = new ArrayList<>();
        if (observations.length == 0) {
            return sampleObservationList;
        }
        double sumValue = 0.0;
        long sumTime = 0L;
        long count = 0L;
        long previousBucket = (observations[0].getOn() - startDate) / bucketTimePeriod;
        for (Observation observation : observations) {
            long curBucket = (observation.getOn() - startDate) / bucketTimePeriod;
            if (curBucket == previousBucket) {
                sumValue = updateSum(sumValue, observation);
                sumTime += observation.getOn();
                count++;
            } else {
                createSample(sumValue, sumTime, count, sampleObservationList);

                //this is the first value of the next bucket
                previousBucket = curBucket;
                sumValue = updateSum(0, observation);
                sumTime = observation.getOn();
                count = 1;
            }
        }
        //last bucket
        createSample(sumValue, sumTime, count, sampleObservationList);

        return sampleObservationList;
    }

    private void createSample(double sumValue, long sumTime, long count, List<List<String>> sampleObservationList) {
        double avgValue = sumValue / count;
        long avgTime = sumTime / count;
        logger.debug("Avgs for bucket for component are: value={} time={} count={}", avgValue, avgTime, count);
        List<String> samples = new ArrayList<>();
        samples.add(String.valueOf(avgTime));
        samples.add(String.valueOf(avgValue));
        sampleObservationList.add(samples);
    }

    private static double updateSum(double value, Observation o) {
        double sum = value;
        try {
            sum += Double.parseDouble(o.getValue());
        } catch (NumberFormatException e) {
            logger.warn("Obs value parsing fault - not a double");
        }
        return sum;
    }

    private static long calculateBucketTimePeriod(DataRetrieveParams dataRetrieveParams) {
        long flooredBucket = (dataRetrieveParams.getEndDate() - dataRetrieveParams.getStartDate())
                / dataRetrieveParams.getMaxPoints();
        return Math.max(flooredBucket, 1);
    }
}
