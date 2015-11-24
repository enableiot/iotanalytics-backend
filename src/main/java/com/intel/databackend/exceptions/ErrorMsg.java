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

package com.intel.databackend.exceptions;


public class ErrorMsg {

    public static final String COUNT_ONLY_WITH_MAX_POINTS_SELECTED = "Illegal combination of parameters. " +
            "Both count only and maxPoints selected.";

    public static final String QUERY_LOCATION_WITH_MAX_POINTS_SELECTED = "Illegal combination of parameters. " +
            "Both queryMeasureLocation and maxPoints selected. Cannot query measure location with bucketing turned on.";

    public static final String DATA_TYPE_NOT_SUPPORTED_FOR_BUCKETING = "Bucketing supported only for number components";

    public static final String NO_COMPONENTS_PROVIDED = "No components provided";

    public static final String NO_START_OR_END_DATE = "startDate and endDate numeric fields are required";

    public static final String NO_START_OR_END_TIMESTAMP = "startTimestamp and endTimestamp numeric fields are required";

    public static final String ZERO_COMPONENT_ROW_LIMIT = "componentRowLimit is lower than the required minimum " +
            "(minimum: 1, found: 0)";

    private ErrorMsg() {
        
    }
}
