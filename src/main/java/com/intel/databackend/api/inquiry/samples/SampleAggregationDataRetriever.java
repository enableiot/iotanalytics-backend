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
    private final long endDate;
    private final long bucketTimePeriod;

    public SampleAggregationDataRetriever(DataRetrieveParams dataRetrieveParams) {
        this.startDate = dataRetrieveParams.getStartDate();
        this.endDate = dataRetrieveParams.getEndDate();
        this.bucketTimePeriod = calculateBucketTimePeriod(dataRetrieveParams);

        logger.debug("Max points = {}", dataRetrieveParams.getMaxPoints());
        logger.debug("bucketTimePeriod = {}", bucketTimePeriod);
    }

    @Override
    public List<List<String>> get(Observation[] observations, Long first, Long last) {
        List<List<String>> sampleObservationList = new ArrayList<>();
        // FIXME - naive, non-optimal - iterates multiple times
        for (long i = startDate; i <= endDate /* FIXME - rounding error compensation? */; i += bucketTimePeriod) {
            logger.debug("bucketing between {} and {}", i, (i + bucketTimePeriod));
            double sumValue = 0.0;
            long sumTime = 0L;
            long count = 0L;
            for (Observation observation : observations) {
                if (observation.getOn() >= i && observation.getOn() < i + bucketTimePeriod) {
                    sumValue = updateSum(sumValue, observation);
                    sumTime += observation.getOn();
                    count++;
                }
            }
            if (count > 0) {
                Double avgValue = sumValue / count;
                Long avgTime = sumTime / count;
                logger.debug("Avgs for bucket for component are: value={} time={} count={}", avgValue, avgTime, count);
                List<String> samples = new ArrayList<>();

                samples.add(avgTime.toString());
                samples.add(avgValue.toString());

                sampleObservationList.add(samples);
            }
        }

        return sampleObservationList;
    }


    private static double updateSum(double sumValue, Observation o) {
        try {
            sumValue += Double.parseDouble(o.getValue());
        } catch (Exception e) {
            logger.warn("Obs value parsing fault - not a double");
        }
        return sumValue;
    }

    private static long calculateBucketTimePeriod(DataRetrieveParams dataRetrieveParams) {
        long flooredBucket = (dataRetrieveParams.getEndDate() - dataRetrieveParams.getStartDate()) / dataRetrieveParams.getMaxPoints();
        return Math.max(flooredBucket, 1);
    }
}
