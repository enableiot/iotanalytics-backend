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

package com.intel.databackend.api.inquiry;

import com.intel.databackend.api.inquiry.advanced.filters.ObservationFilterSelector;
import com.intel.databackend.datasources.hbase.DataDao;
import com.intel.databackend.datastructures.ComponentDataType;
import com.intel.databackend.datastructures.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataRetriever {

    private static final Logger logger = LoggerFactory.getLogger(DataRetriever.class);

    private final DataDao hbase;

    private Map<String, Observation[]> componentObservations;

    private Long rowCount;

    private final DataRetrieveParams dataRetrieveParams;


    public DataRetriever(DataDao hbase, DataRetrieveParams dataRetrieveParams) {
        this.hbase = hbase;
        this.dataRetrieveParams = dataRetrieveParams;
    }

    public void retrieveAndCount(ObservationFilterSelector filter) {
        Map<String, Observation[]> componentObservations = new HashMap<>();
        Collection<String> components = dataRetrieveParams.getComponentsMetadata().keySet();
        rowCount = 0L;
        for (String component : components) {
            Observation[] observations = hbase.scan(dataRetrieveParams.getAccountId(),
                    component,
                    dataRetrieveParams.getStartDate(),
                    dataRetrieveParams.getEndDate(),
                    dataRetrieveParams.isQueryMeasureLocation(),
                    dataRetrieveParams.getComponentsAttributes());
            if (observations == null) {
                logger.debug("No observations retrieved for component: {}", component);
                continue;
            }
            observations = filter.filter(observations, getComponentMetadata(component));
            componentObservations.put(component, observations);
            updateRowCount(observations);
        }
        this.componentObservations = componentObservations;
    }

    public Map<String, Observation[]> getComponentObservations() {
        return componentObservations;
    }

    public Long getRowCount() {
        return rowCount;
    }

    private void updateRowCount(Observation[] obs) {
        rowCount += obs.length;
    }

    private ComponentDataType getComponentMetadata(String component) {
        if (dataRetrieveParams.getComponentsMetadata() != null) {
            return dataRetrieveParams.getComponentsMetadata().get(component);
        }
        return null;
    }
}
