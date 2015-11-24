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

import com.intel.databackend.api.helpers.Common;
import com.intel.databackend.datastructures.Observation;

import java.util.ArrayList;
import java.util.List;

public class SamplePlainDataRetriever implements SampleDataRetriever {

    private List<String> observationAttributes;

    public SamplePlainDataRetriever(List<String> observationAttributes) {
        this.observationAttributes = observationAttributes;
    }

    @Override
    public List<List<String>> get(Observation[] observations, Long first, Long last) {
        ArrayList<List<String>> sampleObservationList = new ArrayList<List<String>>();
        int maxCoordinatesCount = Common.getMaxCoordinatesCount(observations);
        for (Long i = first; i < last; i++) {
            Observation observation = observations[i.intValue()];
            List<String> samples = new ArrayList<String>();
            samples.add(observation.getOn().toString());
            samples.add(observation.getValue());
            sampleObservationList.add(samples);
            Common.addObservationLocation(observation, samples, maxCoordinatesCount);
            addObservationAttributes(observation, samples);
        }
        return sampleObservationList;

    }

    private void addObservationAttributes(Observation observation, List<String> samples) {
        if (observationAttributes != null) {
            for (String attrName : observationAttributes) {
                String attrValue = observation.getAttributes().get(attrName);
                if (attrValue == null) {
                    samples.add("");
                } else {
                    samples.add(attrValue);
                }
            }
        }
    }
}
