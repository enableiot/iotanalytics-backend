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

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.Date;

public class DataFormatterTest {

    @Test
    public void Invoke_DataFormatter_zeroPrefixedTimestamp() {
        String timestamp = DataFormatter.zeroPrefixedTimestamp(new Date().getTime());
        assert StringUtils.isNotEmpty(timestamp);
        assert timestamp.length() == 13;
    }

    @Test
    public void Invoke_DataFormatter_gpsValueToString() {
        assert "locX".equals(DataFormatter.gpsValueToString(0));
        assert "locY".equals(DataFormatter.gpsValueToString(1));
        assert "locZ".equals(DataFormatter.gpsValueToString(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void DataFormatter_gpsValueToString__ThrowsException() {
        DataFormatter.gpsValueToString(3);
    }

    @Test
    public void Invoke_DataFormatter_getTimeFromKey() {
        assert DataFormatter.getTimeFromKey("test \0 datatime \0 1234 \0 key").equals(1234L);
    }

    @Test
    public void Invoke_DataFormatter_fixStopForExclusiveScan() {
        assert  DataFormatter.fixStopForExclusiveScan(99L, 100L) == 101L;
        assert  DataFormatter.fixStopForExclusiveScan(105L, 100L) == 100L;
    }
}
