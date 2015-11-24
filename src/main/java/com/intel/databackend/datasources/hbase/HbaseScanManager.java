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

package com.intel.databackend.datasources.hbase;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class HbaseScanManager {

    private static final Logger logger = LoggerFactory.getLogger(HbaseScanManager.class);
    private Scan scan;
    private final String accountId;
    private final String componentId;

    private static long MAX_DATA_PER_SCAN = 1000;
    private final PageFilter defaultPageFilter;

    public HbaseScanManager(String accountId, String componentId) {
        this.accountId = accountId;
        this.componentId = componentId;
        defaultPageFilter = new PageFilter(MAX_DATA_PER_SCAN);
    }

    public HbaseScanManager create(long start, long stop) {
        stop = DataFormatter.fixStopForExclusiveScan(start, stop);
        scan = new Scan(
                Bytes.toBytes(accountId + '\0' + componentId + '\0' + DataFormatter.zeroPrefixedTimestamp(start)),
                Bytes.toBytes(accountId + '\0' + componentId + '\0' + DataFormatter.zeroPrefixedTimestamp(stop))
        );
        scan.setFilter(defaultPageFilter);
        return this;
    }

    public HbaseScanManager askForData(Boolean gps, String[] attributes) {
        scan.addColumn(Columns.BYTES_COLUMN_FAMILY, Columns.BYTES_DATA_COLUMN);
        askForAdditionalInformation(gps, attributes);
        return this;
    }

    public HbaseScanManager setFilter(Filter filter) {
        if (filter instanceof PageFilter) {
            if (isPageLimitExceeded((PageFilter) filter)) {
                throw new IllegalArgumentException("m");
            }
        }
        scan.setFilter(filter);
        return this;
    }

    public HbaseScanManager setCaching(int limit) {
        scan.setCaching(limit);
        return this;
    }

    public HbaseScanManager setReversed() {
        scan.setReversed(true);
        return this;
    }

    public Scan getScan() {
        return scan;
    }

    private boolean isPageLimitExceeded(PageFilter filter) {
        if (filter.getPageSize() > MAX_DATA_PER_SCAN) {
            return true;
        }
        return false;
    }

    private void askForAdditionalInformation(Boolean gps, String[] attributes) {
        if (attributes != null) {
            askForAttributes(attributes);
        }

        if (gps != null && gps) {
            askForLocation();
        }
    }

    private void askForAttributes(String[] attributes) {
        for (String a : attributes) {
            scan.addColumn(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(Columns.ATTRIBUTE_COLUMN_PREFIX + a));
        }
    }

    private void askForLocation() {
        logger.debug("Retrieving also GPS location");
        for (int i = 0; i < Columns.GPS_COLUMN_SIZE; i++) {
            scan.addColumn(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(DataFormatter.gpsValueToString(i)));
        }
    }
}
