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

package com.intel.databackend.datastructures;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;
import java.util.Map;



@JsonInclude(Include.NON_NULL)
public class Observation {

    public final static String VALUE = "Value";
    public final static String TIMESTAMP = "Timestamp";

    public Observation() {}

    public Observation(String aid, String cid, long on, String value) {
        this.aid = aid;
        this.cid = cid;
        this.on = on;
        this.value = value;
    }

    public Observation(String aid, String cid, long on, String value, List<Double> loc) {
        this(aid, cid, on, value);
        this.loc = loc;
    }

    public Observation(String aid, String cid, long on, String value, Map<String, String> attributes) {
        this(aid, cid, on, value);
        this.attributes = attributes;
    }

    public Observation(String aid, String cid, long on, String value, List<Double> loc, Map<String, String> attributes) {
        this(aid, cid, on, value, loc);
        this.attributes = attributes;
    }

    private String aid;
    private String cid;
    private Long on;
    private String value;
    private List<Double> loc;
    private Map<String, String> attributes;

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public Long getOn() {
        return on;
    }

    public void setOn(Long on) {
        this.on = on;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Double> getLoc() {
        return loc;
    }

    public void setLoc(List<Double> loc) {
        this.loc = loc;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String toString() {
    return JsonWriter.objectToJson(this);
    }

}