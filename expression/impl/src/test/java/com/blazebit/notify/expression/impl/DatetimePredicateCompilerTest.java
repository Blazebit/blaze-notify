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

package com.blazebit.notify.expression.impl;

import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.expression.Expression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DatetimePredicateCompilerTest extends AbstractPredicateCompilerTest {

    private final String expr;
    private final ExpectedExpressionProducer<DatetimePredicateCompilerTest> expectedExpressionProducer;

    public DatetimePredicateCompilerTest(String expr, ExpectedExpressionProducer<DatetimePredicateCompilerTest> expectedExpressionProducer) {
        this.expr = expr;
        this.expectedExpressionProducer = expectedExpressionProducer;
    }

    @Parameters(name = "{1} {2}")
    public static Collection<Object[]> getTestData() {
        Object[][] literals = {
                {
                        "CURRENT_TIMESTAMP",
                        new ExpectedExpressionProducer<DatetimePredicateCompilerTest>() {
                            @Override
                            public Expression getExpectedExpression(DatetimePredicateCompilerTest testInstance) {
                                return testInstance.now();
                            }
                        }
                },
                {
                        "TIMESTAMP('2014-01-01')",
                        new ExpectedExpressionProducer<DatetimePredicateCompilerTest>() {
                            @Override
                            public Expression getExpectedExpression(DatetimePredicateCompilerTest testInstance) {
                                return testInstance.time("2014-01-01");
                            }
                        }
                },
                {
                        "TIMESTAMP('2014-01-01 00:00:00')",
                        new ExpectedExpressionProducer<DatetimePredicateCompilerTest>() {
                            @Override
                            public Expression getExpectedExpression(DatetimePredicateCompilerTest testInstance) {
                                return testInstance.time("2014-01-01 00:00:00");
                            }
                        }
                },
                {
                        "TIMESTAMP('2014-01-01 00:00:00.000')",
                        new ExpectedExpressionProducer<DatetimePredicateCompilerTest>() {
                            @Override
                            public Expression getExpectedExpression(DatetimePredicateCompilerTest testInstance) {
                                return testInstance.time("2014-01-01 00:00:00.000");
                            }
                        }
                },
                {
                        "TIMESTAMP('2014-01-01 01:01:01.100')",
                        new ExpectedExpressionProducer<DatetimePredicateCompilerTest>() {
                            @Override
                            public Expression getExpectedExpression(DatetimePredicateCompilerTest testInstance) {
                                return testInstance.time("2014-01-01 01:01:01.100");
                            }
                        }
                }
        };

        List<Object[]> parameters = new ArrayList<>(literals.length);
        for (int opIdx = 0; opIdx < literals.length; opIdx++) {
            parameters.add(new Object[]{
                    literals[opIdx][0],
                    literals[opIdx][1]
            });
        }

        return parameters;
    }

    @Test
    public void comparisonWithLiteralTest() {
        assertEquals(expectedExpressionProducer.getExpectedExpression(this), parseArithmeticExpression(expr));
    }

    @Override
    protected DomainModel getTestDomainModel() {
        // TODO: define test domain model
        return null;
    }
}
