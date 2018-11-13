/*
 * Copyright 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.notify.expression;

import java.util.Arrays;
import java.util.List;


public enum TermType {
    NUMERIC(ComparisonOperator.values()),
    DATE_TIME(ComparisonOperator.values()),
    STRING(ComparisonOperator.EQUAL, ComparisonOperator.NOT_EQUAL),
    ENUM(ComparisonOperator.EQUAL, ComparisonOperator.NOT_EQUAL),
    COLLECTION();

    private final List<ComparisonOperator> allowedOperatorTypes;

    private TermType(ComparisonOperator... operatorTypes) {
        this.allowedOperatorTypes = Arrays.asList(operatorTypes);
    }

    public List<ComparisonOperator> getAllowedComparisonOperatorTypes() {
        return allowedOperatorTypes;
    }
}
