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
import org.apache.commons.lang.builder.CompareToBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class ObservationComparator implements Comparator<Observation> {

    private final List<Map<String, String>> sort;
    private final ComponentDataType componentDataType;

    private final static String ASC = "Asc";
    private final static String DESC = "Desc";

    public ObservationComparator(List<Map<String, String>> sort, ComponentDataType componentDataType) {
        this.sort = sort;
        this.componentDataType = componentDataType;
    }

    @Override
    public int compare(Observation obs1, Observation obs2) {
        CompareToBuilder compareToBuilder = new CompareToBuilder();
        for (Map<String, String> sortCriterium : sort) {
            for (String key : sortCriterium.keySet()) {
                switch (key) {
                    case Observation.TIMESTAMP:
                        switch (sortCriterium.get(key)) {
                            case ASC:
                                compareToBuilder.append(obs1.getOn(), obs2.getOn());
                                break;
                            case DESC:
                                compareToBuilder.append(obs2.getOn(), obs1.getOn());
                                break;
                        }
                        break;
                    case Observation.VALUE:
                        switch (sortCriterium.get(key)) {
                            case ASC:
                                setSortOrder(obs1, obs2, compareToBuilder, componentDataType);
                                break;
                            case DESC:
                                setSortOrder(obs2, obs1, compareToBuilder, componentDataType);
                                break;
                        }
                        break;
                }

            }

        }
        return compareToBuilder.toComparison();
    }

    private void setSortOrder(Observation obs1, Observation obs2, CompareToBuilder compareToBuilder, ComponentDataType componentDataType) {
        try {
            if (componentDataType.isNumericType()) {
                compareToBuilder.append(Double.parseDouble(obs1.getValue()), Double.parseDouble(obs2.getValue()));
            } else {
                compareToBuilder.append(obs1.getValue(), obs2.getValue());
            }
        } catch (NumberFormatException ex) {
            compareToBuilder.append(obs1.getValue(), obs2.getValue());
        }
    }
}
