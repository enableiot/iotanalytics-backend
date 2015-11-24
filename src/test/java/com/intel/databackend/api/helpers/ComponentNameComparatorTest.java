package com.intel.databackend.api.helpers;

import com.intel.databackend.api.helpers.comparators.ComponentNameComparator;
import com.intel.databackend.datastructures.ComponentDataType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class ComponentNameComparatorTest {
    List<String> list;
    ComponentNameComparator componentNameComparator;
    Map<String, List<String>> map;
    @Before
    public void SetUp(){
        map = new HashMap<>();
        list = new ArrayList<>();
    }

    @Test
    public void Contains_ComponentNameInComponentNameExistsInList_ReturnsTrue() {
        //arrange
        map.put("componentName", list);
        componentNameComparator = new ComponentNameComparator(map);
        list.add(0,"expected");
        ComponentDataType componentDataType = new ComponentDataType();
        componentDataType.setComponentName("expected");
        //act
        boolean result = componentNameComparator.contains(componentDataType);
        //asert
        assert result;
    }

    @Test
    public void Contains_ComponentNameInComponentNameNotExistsInList_ReturnsFalse() {
        //arrange
        map.put("componentName", list);
        componentNameComparator = new ComponentNameComparator(map);
        list.add(0,"not_expected");
        ComponentDataType componentDataType = new ComponentDataType();
        componentDataType.setComponentName("expected");
        boolean result = componentNameComparator.contains(componentDataType);
        assert !result;
    }

    @Test(expected = NullPointerException.class)
    public void Contains_ComponentNameNotInComponentNameNotExistsInList_ReturnsFalse() {
        //arrange
        map.put("componentNameNotIn", list);
        componentNameComparator = new ComponentNameComparator(map);
        list.add(0,"not_expected");
        ComponentDataType componentDataType = new ComponentDataType();
        componentDataType.setComponentName("expected");
        //act
        componentNameComparator.contains(componentDataType);
    }
}
