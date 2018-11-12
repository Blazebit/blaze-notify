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

package com.blazebit.notify.predicate.parser;

import com.blazebit.notify.predicate.model.*;
import org.antlr.v4.runtime.ParserRuleContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;

public abstract class AbstractPredicateCompilerTest {

	protected static Expression parseArithmeticExpression(String input) {
		return PredicateCompiler.parse(input, new PredicateCompiler.RuleInvoker() {
			@Override
			public ParserRuleContext invokeRule(PredicateParser parser) {
				return parser.arithmetic_expression();
			}
		});
	}

	protected static Expression parseStringExpression(String input) {
		return PredicateCompiler.parse(input, new PredicateCompiler.RuleInvoker() {
			@Override
			public ParserRuleContext invokeRule(PredicateParser parser) {
				return parser.string_expression();
			}
		});
	}

	protected static Expression parseDateTimeExpression(String input) {
		return PredicateCompiler.parse(input, new PredicateCompiler.RuleInvoker() {
			@Override
			public ParserRuleContext invokeRule(PredicateParser parser) {
				return parser.datetime_expression();
			}
		});
	}

	protected static DisjunctivePredicate or(Predicate... disjuncts) {
		return new DisjunctivePredicate(Arrays.asList(disjuncts));
	}
	
	protected static ConjunctivePredicate and(Predicate... conjuncts) {
		return new ConjunctivePredicate(Arrays.asList(conjuncts));
	}
	
	protected static DateTimeAtom time(Calendar value) {
		return new DateTimeAtom(Literal.of(value));
	}
	
	protected static DateTimeAtom time(String value) {
		return new DateTimeAtom(Literal.ofDateTimeString(wrapTimestamp(value)));
	}
	
	protected static String wrapTimestamp(String dateTimeStr) {
		return "TIMESTAMP('" + dateTimeStr + "')";
	}
	
	protected static DateTimeAtom now() {
		return new DateTimeAtom(true);
	}
	
	protected static StringAtom string(String value) {
		return new StringAtom(Literal.of(value));
	}
	
	protected static ArithmeticAtom number(long value) {
		return new ArithmeticAtom(Literal.of(new BigDecimal(value)));
	}
	
	protected static ArithmeticAtom number(BigDecimal value) {
		return new ArithmeticAtom(Literal.of(value));
	}
	
	protected static ArithmeticAtom number(String value) {
		return new ArithmeticAtom(Literal.ofNumericString(value));
	}
	
	protected static ArithmeticAtom arithmeticAttr(String identifier) {
		return new ArithmeticAtom(new Attribute(identifier, TermType.NUMERIC));
	}
	
	protected static DateTimeAtom dateTimeAttr(String identifier) {
		return new DateTimeAtom(new Attribute(identifier, TermType.DATE_TIME));
	}
	
	protected static StringAtom stringAttr(String identifier) {
		return new StringAtom(new Attribute(identifier, TermType.STRING));
	}

	protected static EnumAtom enumAttr(String identifier) {
		return new EnumAtom(new Attribute(identifier, TermType.ENUM));
	}

	protected static EnumAtom enumValue(String enumName, String enumKey) {
		return new EnumAtom(Literal.of(new EnumValue(enumName, enumKey)));
	}

	protected static CollectionAtom collectionAttr(String identifier) {
		return new CollectionAtom(new Attribute(identifier, TermType.COLLECTION));
	}
	
	protected static ComparisonPredicate neq(TermExpression left, TermExpression right) {
		return new ComparisonPredicate(left, right, ComparisonOperatorType.NOT_EQUAL);
	}
	
	protected static ComparisonPredicate eq(TermExpression left, TermExpression right) {
		return new ComparisonPredicate(left, right, ComparisonOperatorType.EQUAL);
	}
	
	protected static ComparisonPredicate gt(TermExpression left, TermExpression right) {
		return new ComparisonPredicate(left, right, ComparisonOperatorType.GREATER);
	}
	
	protected static ComparisonPredicate ge(TermExpression left, TermExpression right) {
		return new ComparisonPredicate(left, right, ComparisonOperatorType.GREATER_OR_EQUAL);
	}
	
	protected static ComparisonPredicate lt(TermExpression left, TermExpression right) {
		return new ComparisonPredicate(left, right, ComparisonOperatorType.LOWER);
	}
	
	protected static ComparisonPredicate le(TermExpression left, TermExpression right) {
		return new ComparisonPredicate(left, right, ComparisonOperatorType.LOWER_OR_EQUAL);
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
	
	protected static BetweenPredicate<ArithmeticExpression> between(ArithmeticExpression left, ArithmeticExpression lower, ArithmeticExpression upper) {
		return new ArithmeticBetweenPredicate(left, upper, lower);
	}
	
	protected static BetweenPredicate<DateTimeAtom> between(DateTimeAtom left, DateTimeAtom lower, DateTimeAtom upper) {
		return new DateTimeBetweenPredicate(left, upper, lower);
	}
	
	protected static StringInCollectionPredicate inCollection(StringAtom value, CollectionAtom collection) {
		return new StringInCollectionPredicate(value, collection, false);
	}
	
	protected static StringInPredicate in(StringAtom value, StringAtom... items) {
		return new StringInPredicate(value, Arrays.asList(items), false);
	}
	
	protected static ArithmeticInCollectionPredicate inCollection(ArithmeticExpression value, CollectionAtom collection) {
		return new ArithmeticInCollectionPredicate(value, collection, false);
	}
	
	protected static ArithmeticInPredicate in(ArithmeticExpression value, ArithmeticFactor... items) {
		return new ArithmeticInPredicate(value, Arrays.asList(items), false);
	}

	protected static EnumInCollectionPredicate inCollection(EnumAtom value, CollectionAtom collection) {
		return new EnumInCollectionPredicate(value, collection, false);
	}

	protected static EnumInPredicate in(EnumAtom value, EnumAtom... items) {
		return new EnumInPredicate(value, Arrays.asList(items), false);
	}
}
