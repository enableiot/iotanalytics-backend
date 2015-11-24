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
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.datastructures.requests.DataSubmissionRequest;
import com.intel.databackend.datastructures.responses.DataSubmissionResponse;
import com.intel.databackend.exceptions.MissingDataSubmissionArgumentException;
import org.springframework.beans.factory.annotation.Autowired;


public class DataSubmissionService implements Service<DataSubmissionRequest, DataSubmissionResponse> {

    private String accountId;
    private DataSubmissionRequest request;

    private DataDao dataDao;

    @Autowired
    public DataSubmissionService(DataDao dataDao) {
        this.dataDao = dataDao;
    }

    @Override
    public Service withParams(String accountId, DataSubmissionRequest request) {
        this.accountId = accountId;
        this.request = request;
        return this;
    }

    @Override
    public DataSubmissionResponse invoke() throws MissingDataSubmissionArgumentException {
        if (request.getData() == null) {
            throw new MissingDataSubmissionArgumentException("Missing \"data\" field in request");
        }
        for (Observation o : request.getData()) {
            o.setAid(accountId);
        }

        dataDao.put(request.getData().toArray(new Observation[request.getData().size()]));
        return new DataSubmissionResponse();
    }
}
