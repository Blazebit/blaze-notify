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

import com.blazebit.notify.domain.runtime.model.DomainPredicateType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public enum ComparisonOperator {

    GREATER(">", DomainPredicateType.RELATIONAL),
    GREATER_OR_EQUAL(">=", DomainPredicateType.RELATIONAL),
    LOWER("<", DomainPredicateType.RELATIONAL),
    LOWER_OR_EQUAL("<=", DomainPredicateType.RELATIONAL),
    EQUAL("=", DomainPredicateType.EQUALITY),
    NOT_EQUAL("!=", DomainPredicateType.EQUALITY);

    private static final Map<String, ComparisonOperator> OPERATOR_MAP;

    static {
        Map<String, ComparisonOperator> operator_map = new HashMap<>();

        for (ComparisonOperator operatorType : ComparisonOperator.values()) {
            operator_map.put(operatorType.getOperator(), operatorType);
        }

        OPERATOR_MAP = Collections.unmodifiableMap(operator_map);
    }

    private final String operator;
    private final DomainPredicateType domainPredicateType;

    private ComparisonOperator(String operator, DomainPredicateType domainPredicateType) {
        this.operator = operator;
        this.domainPredicateType = domainPredicateType;
    }

    public String getOperator() {
        return operator;
    }

    public static ComparisonOperator valueOfOperator(String operator) {
        ComparisonOperator operatorType = OPERATOR_MAP.get(operator);
        if (operatorType == null) {
            throw new IllegalArgumentException("Invalid operator: " + operator);
        } else {
            return operatorType;
        }
    }

    public DomainPredicateType getDomainPredicateType() {
        return domainPredicateType;
    }
}
