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

import org.apache.hadoop.hbase.filter.ColumnCountGetFilter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class HbaseScanManagerTest {

    HbaseScanManager hbaseScanManager;
    @Before
    public void SetUp(){
        hbaseScanManager = new HbaseScanManager("accountId", "cid");
        hbaseScanManager.create(99L, 100L);
    }

    @Test
    public void CheckDefaultFilter() {
        assert  hbaseScanManager.getScan().getFilter() instanceof PageFilter;
    }

    @Test
    public void CheckScansAccessorMethods() {
        hbaseScanManager.setFilter(new ColumnCountGetFilter(2));
        assert  hbaseScanManager.getScan().getFilter() instanceof ColumnCountGetFilter;

        hbaseScanManager.setCaching(5);
        assert hbaseScanManager.getScan().getCaching() == 5;

        assert !hbaseScanManager.getScan().isReversed();
        hbaseScanManager.setReversed();
        assert hbaseScanManager.getScan().isReversed();
    }

    @Test(expected = IllegalArgumentException.class)
    public void Invoke_setFilter__ThrowsException() {
        hbaseScanManager.setFilter(new PageFilter(999L));
        assert  hbaseScanManager.getScan().getFilter() instanceof PageFilter;

        hbaseScanManager.setFilter(new PageFilter(10001L));
    }

    @Test
    public void Invoke_askForData() {
        String paramAge = "age";
        String paramCity = "city";

        hbaseScanManager.askForData(true, new String[]{paramAge, paramCity});
        assert hbaseScanManager.getScan().hasFamilies();
        assert hbaseScanManager.getScan().getFamilies().length == 1;
        assert new String(hbaseScanManager.getScan().getFamilies()[0]).equals(Columns.COLUMN_FAMILY);

        List<String> result = hbaseScanManager.getScan().getFamilyMap()
                .get(Columns.BYTES_COLUMN_FAMILY).stream().map(String::new).collect(Collectors.toList());

        assert result.indexOf(Columns.ATTRIBUTE_COLUMN_PREFIX+paramAge) > -1;
        assert result.indexOf(Columns.ATTRIBUTE_COLUMN_PREFIX+paramCity) > -1;
    }

}
