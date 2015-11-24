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

package com.intel.databackend.api.helpers.comparators;

import com.intel.databackend.datastructures.ComponentDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class ComponentTypeComparator implements ComponentComparator {

    private static final Logger logger = LoggerFactory.getLogger(ComponentTypeComparator.class);


    private final List<String> componentTypes;

    public ComponentTypeComparator(Map<String, List<String>> filterContent) {
        this.componentTypes = filterContent.get("componentType");
        logger.debug("Filtering by componentType");
    }

    @Override
    public boolean contains(ComponentDataType component) {
        return componentTypes.contains(component.getComponentType());
    }
}
