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

package com.intel.databackend.api;

import com.intel.databackend.datasources.hbase.DataDao;
import com.intel.databackend.datastructures.ComponentMeasurementTimestamps;
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.datastructures.requests.FirstLastTimestampRequest;
import com.intel.databackend.datastructures.responses.FirstLastTimestampResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

@org.springframework.stereotype.Service
@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
public class FirstLastTimestampService implements Service<FirstLastTimestampRequest, FirstLastTimestampResponse> {

    private String accountId;
    private FirstLastTimestampRequest request;

    private DataDao hbase;

    private FirstLastTimestampResponse response;

    @Autowired
    public FirstLastTimestampService(DataDao hBase) {
        this.hbase = hBase;
        this.response = new FirstLastTimestampResponse();
    }

    @Override
    public Service withParams(String accountId, FirstLastTimestampRequest request) {
        this.accountId = accountId;
        this.request = request;
        return this;
    }

    @Override
    public FirstLastTimestampResponse invoke() {
        response.setComponentsFirstLast(new ArrayList<>());
        for (String componentId : request.getComponents()) {
            ComponentMeasurementTimestamps componentMeasurementTimestamps = new ComponentMeasurementTimestamps();
            componentMeasurementTimestamps.setComponentId(componentId);
            Observation[] observations = getTopObservation(componentId, true);
            if (observations == null || observations.length == 0) {
                continue;
            }

            componentMeasurementTimestamps.setFirstPointTS(observations[0].getOn());
            Observation[] obs2 = getTopObservation(componentId, false);
            if (obs2 == null || obs2.length == 0) {
                continue;
            }

            componentMeasurementTimestamps.setLastPointTS(obs2[0].getOn());
            response.getComponentsFirstLast().add(componentMeasurementTimestamps);
        }

        return response;
    }

    private Observation[] getTopObservation(String component, boolean first) {
        return hbase.scan(accountId, component,
                0L,
                Long.MAX_VALUE,
                false,
                null,
                first,
                1);
    }


}
