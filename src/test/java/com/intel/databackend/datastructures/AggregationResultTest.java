package com.intel.databackend.datastructures;

import com.intel.databackend.api.inquiry.advanced.aggregations.AggregationResult;
import org.junit.Before;
import org.junit.Test;

/**
 * Copyright (c) 2015 Intel Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class AggregationResultTest {
    AggregationResult aggregationResult;

    @Before
    public void SetUp() {
        aggregationResult = new AggregationResult();
    }

    @Test
    public void addToComponent_fillsComponentWithOwnData() {
        //arrange
        AdvancedComponent advancedComponent = new AdvancedComponent();
        aggregationResult.setMax(1.0);
        aggregationResult.setMin(1.0);
        aggregationResult.setCount(1);
        aggregationResult.setSum(1.0);
        aggregationResult.setSumOfSquares(1.0);

        //act
        aggregationResult.addToComponent(advancedComponent);

        //assert
        assert advancedComponent.getMax() == 1.0;
        assert advancedComponent.getMin() == 1.0;
        assert advancedComponent.getCount() == 1;
        assert advancedComponent.getSum() == 1.0;
        assert advancedComponent.getSumOfSquares() == 1.0;

    }
}
