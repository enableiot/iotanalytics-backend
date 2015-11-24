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

package com.intel.databackend.api.helpers;

import com.intel.databackend.api.helpers.comparators.ComponentComparator;
import com.intel.databackend.api.helpers.comparators.ComponentNameComparator;
import com.intel.databackend.api.helpers.comparators.ComponentTypeComparator;
import com.intel.databackend.datasources.dashboard.components.ComponentsDao;
import com.intel.databackend.datasources.dashboard.utils.QueryFields;
import com.intel.databackend.datasources.dashboard.utils.QueryParam;
import com.intel.databackend.datastructures.ComponentDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ComponentFilters {

    private static final Logger logger = LoggerFactory.getLogger(ComponentFilters.class);
    private static final String COMPONENT_NAME = "componentName";
    private static final String COMPONENT_TYPE = "componentType";
    private static final String DEVICE_NAME = "deviceName";
    private static final String TAGS = "Tags";
    private static final String[] FILTER_TYPE = {COMPONENT_NAME, COMPONENT_TYPE, DEVICE_NAME, TAGS};

    private ComponentsDao componentsDao;

    private Map<String, List<String>> filterContent;

    private ComponentComparator componentComparator;

    private List<String> filterResult;

    public ComponentFilters(ComponentsDao componentsDao, Map<String, List<String>> filterContent) {
        this.componentsDao = componentsDao;
        this.filterContent = filterContent;
    }

    public boolean isFilterAvailable() {
        if (filterContent == null) {
            return false;
        }
        for (String type: FILTER_TYPE) {
            if (filterContent.containsKey(type)) {
                return true;
            }
        }
        return false;
    }

    public List<String> filter(Map<String, ComponentDataType> componentsMetadata, List<String> componentIds, String accountId) {
        filterResult = new ArrayList<String>();
        if (filterContent != null) {
            if (filterContent.containsKey(COMPONENT_NAME)) {
                componentComparator = new ComponentNameComparator(this.filterContent);
                addFilterResult(filterByComponentParam(componentsMetadata));
            }
            if (filterContent.containsKey(COMPONENT_TYPE)) {
                componentComparator = new ComponentTypeComparator(this.filterContent);
                addFilterResult(filterByComponentParam(componentsMetadata));
            }
            if (filterContent.containsKey(DEVICE_NAME) || filterContent.containsKey(TAGS)) {
                addFilterResult(filterByDeviceNameOrTags(accountId, componentIds, filterContent));
            }
        }

        return filterResult;
    }

    private void addFilterResult(List<String> result) {
        if (filterResult.size() > 0) {
            filterResult.retainAll(result);
        } else {
            filterResult.addAll(result);
        }
    }

    public List<String> filterByComponentParam(Map<String, ComponentDataType> componentsMetadata) {
        List<String> filtered = new ArrayList<String>();
        for (ComponentDataType component : componentsMetadata.values()) {
            if (componentComparator.contains(component)) {
                filtered.add(component.getComponentId());
            } else {
                logger.debug("Omitting component {} because its name or type is not in accepted list.", component.getComponentId());
            }
        }
        return filtered;
    }

    public List<String> filterByDeviceNameOrTags(String accountId, List<String> componentIds, Map<String, List<String>> devCompAttributeFilter) {
        logger.debug("Filtering by device names or tags");
        List<String> deviceName = null;
        List<String> deviceTags = null;
        if (devCompAttributeFilter.containsKey("deviceName")) {
            deviceName = devCompAttributeFilter.get("deviceName");
            logger.debug("Accepted device names: {}", deviceName);
        }
        if (devCompAttributeFilter.containsKey("Tags")) {
            deviceTags = devCompAttributeFilter.get("Tags");
            logger.debug("Accepted device tags: {}", deviceTags);
        }

        List<String> componentsFromDeviceNamesOrTags = componentsDao.getComponentsByCustomParams(accountId, createFilterParams(deviceName, deviceTags));

        componentIds.retainAll(componentsFromDeviceNamesOrTags);
        logger.debug("Result of retainAll {}", componentIds);

        return componentIds;
    }

    private static List<QueryParam> createFilterParams(List<String> names, List<String> tags) {
        List<QueryParam> filters = new ArrayList<>();
        if (names != null && names.size() > 0) {
            filters.add(new QueryParam(QueryFields.DEVICE_NAME, names));
        }
        if (tags != null && tags.size() > 0) {
            filters.add(new QueryParam(QueryFields.DEVICE_TAG, tags));
        }
        return filters;
    }
}
