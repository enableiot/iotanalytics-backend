package com.intel.databackend.datasources.hbase;

import com.intel.databackend.datastructures.Observation;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) 2015 Intel Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ObservationCreator {

    private Observation observation;
    private static final Logger logger = LoggerFactory.getLogger(ObservationCreator.class);
    private final String accountId;
    private final String componentId;
    private Result result;
    private Boolean hasGps;
    private String[] attributes;

    ObservationCreator(String accountId, String componentId) {
        this.accountId = accountId;
        this.componentId = componentId;
    }

    public ObservationCreator withGps(boolean hasGps) {
        this.hasGps = hasGps;
        return this;
    }

    public ObservationCreator withAttributes(String... attributes) {
        this.attributes = attributes;
        return this;
    }

    public Observation create(Result result) {
        observation = new Observation();
        this.result = result;
        addBasicInformation();
        addAdditionalInformation();
        return observation;
    }

    private void addBasicInformation() {
        String key = Bytes.toString(result.getRow());
        String value = Bytes.toString(result.getValue(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(Columns.DATA_COLUMN)));
        observation.setCid(componentId);
        observation.setAid(accountId);
        observation.setOn(DataFormatter.getTimeFromKey(key)); //0L;
        observation.setValue(value);
        observation.setAttributes(new HashMap<String, String>());
    }

    private void addAdditionalInformation() {
        if (attributes != null) {
            addAttributesData(attributes);
        }
        if (hasGps != null && hasGps) {
            addLocationData();
        }
    }

    private void addAttributesData(String[] attributes) {
        for (String a : attributes) {
            String attribute = Bytes.toString(result.getValue(Columns.BYTES_COLUMN_FAMILY,
                    Bytes.toBytes(Columns.ATTRIBUTE_COLUMN_PREFIX + a)));
            observation.getAttributes().put(a, attribute);
        }
    }

    private void addLocationData() {
        try {
            String[] coordinate = new String[Columns.GPS_COLUMN_SIZE];
            observation.setLoc(new ArrayList<Double>());
            for (int i = 0; i < Columns.GPS_COLUMN_SIZE; i++) {
                coordinate[i] = Bytes.toString(result.getValue(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(DataFormatter.gpsValueToString(i))));
                if (coordinate[i] != null) {
                    observation.getLoc().add(Double.parseDouble(coordinate[i]));
                }
            }
        } catch (NumberFormatException e) {
            logger.warn("problem with parsing GPS coords... not a Double?");
        }
    }
}
