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

package com.blazebit.notify.expression.persistence;

import com.blazebit.notify.domain.runtime.model.DomainOperator;
import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.expression.ComparisonOperator;
import com.blazebit.notify.expression.spi.ComparisonOperatorInterpreter;
import com.blazebit.notify.expression.spi.DomainOperatorInterpreter;

public class BooleanOperatorHandler implements ComparisonOperatorInterpreter, DomainOperatorInterpreter {

    public static final BooleanOperatorHandler INSTANCE = new BooleanOperatorHandler();

    private BooleanOperatorHandler() {
    }

    @Override
    public Boolean interpret(DomainType leftType, DomainType rightType, Object leftValue, Object rightValue, ComparisonOperator operator) {
        if (leftValue instanceof Boolean && rightValue instanceof Boolean) {
            boolean l = (Boolean) leftValue;
            boolean r = (Boolean) rightValue;
            switch (operator) {
                case EQUAL:
                    return l == r;
                case NOT_EQUAL:
                    return l != r;
            }
        } else {
            throw new IllegalArgumentException("Illegal arguments [" + leftValue + ", " + rightValue + "]!");
        }


        throw new IllegalArgumentException("Can't handle the operator " + operator + " for the arguments [" + leftValue + ", " + rightValue + "]!");
    }

    @Override
    public Object interpret(DomainType targetType, DomainType leftType, DomainType rightType, Object leftValue, Object rightValue, DomainOperator operator) {
        if (operator == DomainOperator.NOT) {
            return !((boolean) leftValue);
        }

        throw new IllegalArgumentException("Can't handle the operator " + operator + " for the arguments [" + leftValue + ", " + rightValue + "]!");
    }
}
