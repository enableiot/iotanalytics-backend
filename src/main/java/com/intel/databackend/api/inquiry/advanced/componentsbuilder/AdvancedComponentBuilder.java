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

package com.intel.databackend.api.inquiry.advanced.componentsbuilder;

import com.intel.databackend.api.helpers.Common;
import com.intel.databackend.api.inquiry.advanced.aggregations.AggregationCalculator;
import com.intel.databackend.api.inquiry.advanced.aggregations.AggregationResult;
import com.intel.databackend.api.inquiry.samples.SampleDataRetriever;
import com.intel.databackend.api.inquiry.samples.SamplePlainDataRetriever;
import com.intel.databackend.datastructures.AdvancedComponent;
import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdvancedComponentBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedComponentBuilder.class);

    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String ALT = "alt";

    private static final int ONE_COORDINATE = 1;
    private static final int TWO_COORDINATES = 2;
    private static final int THREE_COORDINATES = 3;

    private final Observation[] observations;

    private final Map<String, ComponentDataType> componentsMetadata;

    private final ComponentsBuilderParams parameters;

    public AdvancedComponentBuilder(Observation[] observations, Map<String, ComponentDataType> componentsMetadata,
                                    ComponentsBuilderParams parameters) {
        this.observations = observations;
        this.componentsMetadata = componentsMetadata;
        this.parameters = parameters;
    }

    public void appendAggregations(AdvancedComponent component, Long first) {
        Long last = countUpperLimit(first, parameters.getComponentRowLimit());
        if (AggregationCalculator.includeAggregation(parameters.getAggregations())) {
            logger.debug("Add aggregations for component {} from: {}, to: {}", component.getComponentId(), first, last);

            AggregationCalculator aggregationCalculator =
                    new AggregationCalculator(component.getComponentId(), observations, componentsMetadata);
            AggregationResult aggregationResult = aggregationCalculator.generateAggregations(first, last);
            aggregationResult.addToComponent(component);
        }
    }

    public void appendSamples(AdvancedComponent component, Long first) {
        Long last = countUpperLimit(first, parameters.getComponentRowLimit());
        if (!AggregationCalculator.returnsAggregationOnly(parameters.getAggregations())) {
            logger.debug("Add samples for component {} from - {}, to - {}", component.getComponentId(), first, last);

            SampleDataRetriever sampleDataRetriever =
                    new SamplePlainDataRetriever(parameters.getReturnedMeasureAttributes());
            component.setSamples(sampleDataRetriever.get(observations, first, last));
            component.setSamplesHeader(prepareSamplesHeader());
        }
    }

    private List<String> prepareSamplesHeader() {
        List<String> samplesHeader = new ArrayList<>();
        samplesHeader.add(Observation.TIMESTAMP);
        samplesHeader.add(Observation.VALUE);
        if (observations != null && observations.length > 0) {
            int maxCoordinatesCount = Common.getMaxCoordinatesCount(observations);
            if (maxCoordinatesCount >= ONE_COORDINATE) {
                samplesHeader.add(LAT);
            }
            if (maxCoordinatesCount >= TWO_COORDINATES) {
                samplesHeader.add(LON);
            }
            if (maxCoordinatesCount >= THREE_COORDINATES) {
                samplesHeader.add(ALT);
            }
        }
        if (parameters.getReturnedMeasureAttributes() != null) {
            for (String attrKey : parameters.getReturnedMeasureAttributes()) {
                samplesHeader.add(attrKey);
            }
        }
        return samplesHeader;
    }

    private Long countUpperLimit(Long first, Long componentRowLimit) {
        Long last = first;
        if (componentRowLimit == null) {
            last = (long) observations.length;
        } else {
            last += componentRowLimit;
            if (last > observations.length) {
                last = (long) observations.length;
            }
        }
        return last;
    }
}
