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

package com.intel.databackend.datastructures.requests;

import com.cedarsoftware.util.io.*;
import com.intel.databackend.datastructures.Observation;

import javax.validation.constraints.NotNull;
import java.util.List;



public class DataSubmissionRequest {

    private String accountId;
    private String did;
    private Long on;
    private Long count;
    private Long systemOn;

    @NotNull
    private List<Observation> data =  null;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public Long getOn() {
        return on;
    }

    public void setOn(Long on) {
        this.on = on;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getSystemOn() {
        return systemOn;
    }

    public void setSystemOn(Long systemOn) {
        this.systemOn = systemOn;
    }

    public List<Observation> getData() {
        return data;
    }

    public void setData(List<Observation> data) {
        this.data = data;
    }

    public String toString() {
      return JsonWriter.objectToJson(this);
    }

}