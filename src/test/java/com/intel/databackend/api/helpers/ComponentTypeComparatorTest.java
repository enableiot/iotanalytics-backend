package com.intel.databackend.api.helpers;

import com.intel.databackend.api.helpers.comparators.ComponentTypeComparator;
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
public class ComponentTypeComparatorTest {
    List<String> list;
    ComponentTypeComparator componentTypeComparator;
    Map<String, List<String>> map;

    @Before
    public void SetUp(){
        map = new HashMap<>();
        list = new ArrayList<>();
    }

    @Test
    public void Contains_ComponentTypeInMapComponentNameExistsInList_ReturnsTrue() {
        //arange
        map.put("componentType", list);
        componentTypeComparator = new ComponentTypeComparator(map);
        list.add(0,"expected");
        ComponentDataType componentDataType = new ComponentDataType();
        componentDataType.setComponentType("expected");

        //act
        boolean result = componentTypeComparator.contains(componentDataType);

        //asert
        assert result;
    }

    @Test
    public void Contains_ComponentTypeInMapComponentNameNotExistsInList_ReturnsFalse() {
        //arange
        map.put("componentType", list);
        componentTypeComparator = new ComponentTypeComparator(map);
        list.add(0,"not_expected");
        ComponentDataType componentDataType = new ComponentDataType();
        componentDataType.setComponentType("expected");

        //act
        boolean result = componentTypeComparator.contains(componentDataType);

        //asert
        assert !result;
    }

    @Test(expected = NullPointerException.class)
    public void Contains_ComponentTypeNotInMapComponentNameExistsInList_ThrowsException() {
        //arange
        map.put("componentTypeNot", list);
        componentTypeComparator = new ComponentTypeComparator(map);
        list.add(0,"expected");
        ComponentDataType componentDataType = new ComponentDataType();
        componentDataType.setComponentType("expected");

        //act
        componentTypeComparator.contains(componentDataType);
    }
}
