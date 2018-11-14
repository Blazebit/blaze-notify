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
import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.expression.*;
import org.antlr.v4.runtime.ParserRuleContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

public abstract class AbstractExpressionCompilerTest {

    private final ExpressionCompilerImpl expressionCompiler;

    protected AbstractExpressionCompilerTest() {
        this.expressionCompiler = new ExpressionCompilerImpl(getTestDomainModel());
    }

    protected abstract DomainModel getTestDomainModel();

    protected ExpressionCompiler.Context getCompileContext() {
        return expressionCompiler.createContext(Collections.<String, DomainType>emptyMap());
    }

    protected Predicate parsePredicate(String input) {
        return expressionCompiler.createPredicate(input, getCompileContext());
    }

    protected Expression parseArithmeticExpression(String input) {
        return expressionCompiler.parse(input, new ExpressionCompilerImpl.RuleInvoker() {
            @Override
            public ParserRuleContext invokeRule(PredicateParser parser) {
                return parser.arithmetic_expression();
            }
        }, getCompileContext());
    }

    protected static DisjunctivePredicate or(Predicate... disjuncts) {
        return new DisjunctivePredicate(Arrays.asList(disjuncts));
    }

    protected static ConjunctivePredicate and(Predicate... conjuncts) {
        return new ConjunctivePredicate(Arrays.asList(conjuncts));
    }

    protected Atom time(Calendar value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofCalendar(value));
    }

    protected Atom time(String value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofDateTimeString(value));
    }

    protected static String wrapTimestamp(String dateTimeStr) {
        return "TIMESTAMP('" + dateTimeStr + "')";
    }

    protected Atom now() {
        return new Atom(expressionCompiler.getLiteralFactory().currentTimestamp());
    }

    protected Atom string(String value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofString(value));
    }

    protected Atom number(long value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofBigDecimal(new BigDecimal(value)));
    }

    protected Atom number(BigDecimal value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofBigDecimal(value));
    }

    protected Atom number(String value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofNumericString(value));
    }

    protected Atom attr(String identifier) {
        return new Atom(new Attribute(identifier, getTestDomainModel().getType(identifier)));
    }

    protected Atom enumValue(String enumName, String enumKey) {
        return new Atom(expressionCompiler.getLiteralFactory().ofEnumValue(new EnumValue(enumName, enumKey)));
    }

//	protected static CollectionAtom collectionAttr(String identifier) {
//		return new CollectionAtom(new Attribute(identifier, TermType.COLLECTION));
//	}

    protected static ComparisonPredicate neq(TermExpression left, TermExpression right) {
        return new ComparisonPredicate(left, right, ComparisonOperator.NOT_EQUAL);
    }

    protected static ComparisonPredicate eq(TermExpression left, TermExpression right) {
        return new ComparisonPredicate(left, right, ComparisonOperator.EQUAL);
    }

    protected static ComparisonPredicate gt(TermExpression left, TermExpression right) {
        return new ComparisonPredicate(left, right, ComparisonOperator.GREATER);
    }

    protected static ComparisonPredicate ge(TermExpression left, TermExpression right) {
        return new ComparisonPredicate(left, right, ComparisonOperator.GREATER_OR_EQUAL);
    }

    protected static ComparisonPredicate lt(TermExpression left, TermExpression right) {
        return new ComparisonPredicate(left, right, ComparisonOperator.LOWER);
    }

    protected static ComparisonPredicate le(TermExpression left, TermExpression right) {
        return new ComparisonPredicate(left, right, ComparisonOperator.LOWER_OR_EQUAL);
    }

    protected static ArithmeticFactor pos(ArithmeticExpression expression) {
        return new ArithmeticFactor(expression, false);
    }

    protected static ArithmeticFactor neg(ArithmeticExpression expression) {
        return new ArithmeticFactor(expression, true);
    }

    protected static ArithmeticExpression plus(ArithmeticExpression left, ArithmeticExpression right) {
        return new ChainingArithmeticExpression(left, right, ArithmeticOperatorType.PLUS);
    }

    protected static ArithmeticExpression minus(ArithmeticExpression left, ArithmeticExpression right) {
        return new ChainingArithmeticExpression(left, right, ArithmeticOperatorType.MINUS);
    }

    protected static BetweenPredicate between(ArithmeticExpression left, ArithmeticExpression lower, ArithmeticExpression upper) {
        return new BetweenPredicate(left, upper, lower);
    }

//	protected static StringInCollectionPredicate inCollection(StringAtom value, CollectionAtom collection) {
//		return new StringInCollectionPredicate(value, collection, false);
//	}

    protected static InPredicate in(ArithmeticExpression value, ArithmeticExpression... items) {
        return new InPredicate(value, Arrays.asList(items), false);
    }

    interface ExpectedExpressionProducer<T extends AbstractExpressionCompilerTest> {
        Expression getExpectedExpression(T testInstance);
    }
}
