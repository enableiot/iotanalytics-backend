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

import com.intel.databackend.datastructures.Observation;

import java.io.IOException;

public interface DataDao {

    boolean createTables() throws IOException;

    boolean put(Observation[] o);

    Observation[] scan(String accountId, String componentId, long start, long stop, Boolean gps, String[] attributes);

    Observation[] scan(String accountId, String componentId, long start, long stop, Boolean gps, String[] attributes, boolean forward, int limit);

    String[] scanForAttributeNames(String accountId, String componentId, long start, long stop) throws IOException;
}
