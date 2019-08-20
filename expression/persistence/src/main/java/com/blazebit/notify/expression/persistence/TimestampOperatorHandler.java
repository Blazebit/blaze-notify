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
import com.blazebit.notify.domain.runtime.model.TemporalInterval;
import com.blazebit.notify.expression.ComparisonOperator;
import com.blazebit.notify.expression.persistence.util.ConversionUtils;
import com.blazebit.notify.expression.spi.ComparisonOperatorInterpreter;
import com.blazebit.notify.expression.spi.DomainOperatorInterpreter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public class TimestampOperatorHandler implements ComparisonOperatorInterpreter, DomainOperatorInterpreter {

    public static final TimestampOperatorHandler INSTANCE = new TimestampOperatorHandler();

    private TimestampOperatorHandler() {
    }

    @Override
    public Boolean interpret(DomainType leftType, DomainType rightType, Object leftValue, Object rightValue, ComparisonOperator operator) {
        if (leftValue instanceof Instant && rightValue instanceof Instant) {
            Instant l = (Instant) leftValue;
            Instant r = (Instant) rightValue;
            switch (operator) {
                case EQUAL:
                    return l.compareTo(r) == 0;
                case NOT_EQUAL:
                    return l.compareTo(r) != 0;
                case GREATER_OR_EQUAL:
                    return l.compareTo(r) > -1;
                case GREATER:
                    return l.compareTo(r) > 0;
                case LOWER_OR_EQUAL:
                    return l.compareTo(r) < 1;
                case LOWER:
                    return l.compareTo(r) < 0;
            }
        } else {
            throw new IllegalArgumentException("Illegal arguments [" + leftValue + ", " + rightValue + "]!");
        }

        throw new IllegalArgumentException("Can't handle the operator " + operator + " for the arguments [" + leftValue + ", " + rightValue + "]!");
    }

    @Override
    public Object interpret(DomainType targetType, DomainType leftType, DomainType rightType, Object leftValue, Object rightValue, DomainOperator operator) {
        if (leftValue instanceof TemporalInterval && rightValue instanceof TemporalInterval) {
            TemporalInterval interval1 = (TemporalInterval) leftValue;
            TemporalInterval interval2 = (TemporalInterval) rightValue;

            switch (operator) {
                case PLUS:
                    return interval1.add(interval2);
                case MINUS:
                    return interval1.subtract(interval2);
            }
        } else if (leftValue instanceof Instant && rightValue instanceof TemporalInterval) {
            Instant instant = (Instant) leftValue;
            TemporalInterval interval = (TemporalInterval) rightValue;
            switch (operator) {
                case PLUS:
                    return interval.add(instant);
                case MINUS:
                    return interval.subtract(instant);
            }
        } else if (leftValue instanceof TemporalInterval && rightValue instanceof Instant) {
            TemporalInterval interval = (TemporalInterval) leftValue;
            Instant instant = (Instant) rightValue;

            switch (operator) {
                case PLUS:
                    return interval.add(instant);
                case MINUS:
                    return interval.subtract(instant);
            }
        } else {
            throw new IllegalArgumentException("Illegal arguments [" + leftValue + ", " + rightValue + "]!");
        }

        throw new IllegalArgumentException("Can't handle the operator " + operator + " for the arguments [" + leftValue + ", " + rightValue + "]!");
    }
}