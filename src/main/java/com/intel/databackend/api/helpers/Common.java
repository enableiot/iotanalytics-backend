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

package com.intel.databackend.api.helpers;

import com.intel.databackend.datastructures.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class Common {

    private static final Logger logger = LoggerFactory.getLogger(Common.class);

    private Common(){
    }

    public static void addObservationLocation(Observation observation, List<String> samples, int maxCoordinatesCount) {
        try {
            Double coordinate = null;
            for (int i = 0; i < maxCoordinatesCount; i++) {
                if (observation.getLoc().size() > i) {
                    coordinate = observation.getLoc().get(i);
                    if (coordinate != null) {
                        samples.add(coordinate.toString());
                    } else {
                        samples.add("");
                    }
                } else {
                    samples.add("");
                }
            }
        } catch (Exception e) {
            logger.warn("Parsing GPS failed");
        }
    }

    public static int getMaxCoordinatesCount(Observation[] observations) {
        int maxCoordinatesCount = 0;
        for(Observation observation : observations) {
            List<Double> loc = observation.getLoc();
            if (loc != null) {
                if (loc.size() > maxCoordinatesCount) {
                    maxCoordinatesCount = loc.size();
                }
            }
        }
        return maxCoordinatesCount;
    }
}
