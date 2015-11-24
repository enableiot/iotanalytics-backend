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

import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ObservationFilters {

    private static final Logger logger = LoggerFactory.getLogger(ObservationFilters.class);

    public Observation[] filterByMeasurementAttrs(Observation[] obs, Map<String, List<String>> measurementAttributeFilter) {
        logger.debug("Filtering by measure attributes");
        List<Observation> filtered = new ArrayList<Observation>();
        for (Observation o : obs) {
            boolean omit = false;
            for (String key : measurementAttributeFilter.keySet()) {
                Map<String, String> attributes = o.getAttributes();
                if (!attributes.containsKey(key) || !measurementAttributeFilter.get(key).contains(attributes.get(key))) {
                    omit = true;
                    break;
                }
            }
            if(!omit) {
                filtered.add(o);
            } else {
                continue;
            }
        }
        obs = filtered.toArray(new Observation[filtered.size()]);
        return obs;
    }

    public Observation[] filterByValue(Observation[] obs, Map<String, List<String>> valueFilter, ComponentDataType componentDataType) {
        logger.debug("Filtering by value ");
        List<String> acceptedValues = valueFilter.get("value");
        logger.debug("Accepted values: {}", acceptedValues);
        List<Observation> filtered = new ArrayList<Observation>();
        for (Observation o : obs) {
            // Production did not take care of dataType. Tests treat it as expected.
            if (componentDataType != null) {
                addWhenNumbersEqual(acceptedValues, filtered, o);
            } else {
                addWhenStringEqual(acceptedValues, filtered, o);
            }
        }
        obs = filtered.toArray(new Observation[filtered.size()]);
        return obs;
    }

    private void addWhenNumbersEqual(List<String> acceptedValues, List<Observation> filtered, Observation o) {
        try {
            Double current = Double.parseDouble(o.getValue());
            for (String val : acceptedValues) {
                try {
                    Double accepted = Double.parseDouble(val);
                    if (accepted.equals(current)) {
                        filtered.add(o);
                        break;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Parsing accepted value - {} to double failed.", val);
                }
            }
        } catch (NumberFormatException e) {
            logger.warn("Parsing stored value - {} to double failed.", o.getValue());
            addWhenStringEqual(acceptedValues, filtered, o);
        }
    }

    private void addWhenStringEqual(List<String> acceptedValues, List<Observation> filtered, Observation o) {
        if (acceptedValues.contains(o.getValue())) {
            filtered.add(o);
        }
    }
}
