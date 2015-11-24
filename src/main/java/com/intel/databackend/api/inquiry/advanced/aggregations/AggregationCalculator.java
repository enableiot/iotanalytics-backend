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

package com.intel.databackend.api.inquiry.advanced.aggregations;

import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AggregationCalculator {

    private static final Logger logger = LoggerFactory.getLogger(AggregationCalculator.class);

    private final String componentId;

    private final Map<String, ComponentDataType> componentsMetadata;

    private final Observation[] observations;

    public AggregationCalculator(String componentId, Observation[] observations, Map<String, ComponentDataType> componentsMetadata) {
        this.componentId = componentId;
        this.observations = observations;
        this.componentsMetadata = componentsMetadata;
    }

    public static boolean returnsAggregationOnly(String aggregationMode) {
        return aggregationMode != null && aggregationMode.equals("only");
    }

    public static boolean includeAggregation(String aggregationMode) {
        return aggregationMode != null && (aggregationMode.equals("only") || aggregationMode.equals("include"));
    }

    public AggregationResult generateAggregations(Long first, Long last) {
        logger.debug("Generating aggregations for componentId - {} . From - {}, to - {}", componentId, first, last);

        AggregationResult aggregationResult = new AggregationResult(componentId);
        aggregationResult.setCount(last - first);
        if (aggregationResult.getCount() > 0) {
            if (componentsMetadata.containsKey(componentId)) {
                ComponentDataType componentDataType = componentsMetadata.get(componentId);
                if (componentDataType.isNumericType()) {
                    double min = Double.MAX_VALUE;
                    double max = Double.MIN_VALUE;
                    double sum = 0.0;
                    double sumOfSquares = 0.0;
                    for (Long l = first; l < last; l++) {
                        Observation observation = observations[l.intValue()];
                        try {
                            Double value = Double.parseDouble(observation.getValue());
                            if (value < min) {
                                min = value;
                            }
                            if (value > max) {
                                max = value;
                            }
                            sum += value;
                            sumOfSquares += value * value;
                        } catch (NumberFormatException ex) {
                            logger.warn("Observation value - {} is not a double.", observation.getValue());
                        }
                    }
                    aggregationResult.setMin(min);
                    aggregationResult.setMax(max);
                    aggregationResult.setSum(sum);
                    aggregationResult.setSumOfSquares(sumOfSquares);
                }
            } else {
                logger.warn("ComponentType not found for componentId - {}.", componentId);
            }
        } else {
            logger.warn("No aggregation generated for componentId - {}.", componentId);
        }

        return aggregationResult;
    }
}