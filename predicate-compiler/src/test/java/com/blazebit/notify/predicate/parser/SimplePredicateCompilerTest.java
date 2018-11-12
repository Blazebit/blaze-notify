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

import com.blazebit.notify.predicate.model.Predicate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimplePredicateCompilerTest extends AbstractPredicateCompilerTest {

	@Test
	public void testAddition() {
		Predicate predicate = PredicateCompiler.parsePredicate("1>2+3");
		assertEquals(
				gt(pos(number(1l)), plus(pos(number(2l)), pos(number(3l)))),
				predicate
		);
	}
	
	@Test
	public void testAdditionParenthesis() {
		Predicate predicate = PredicateCompiler.parsePredicate("1>(2+3)");
		assertEquals(
				gt(pos(number(1l)), pos(plus(pos(number(2l)), pos(number(3l))))),
				predicate
		);
	}
	
	@Test
	public void testAdditionParenthesisNegated() {
		Predicate predicate = PredicateCompiler.parsePredicate("1>-(2+3)");
		assertEquals(
				gt(pos(number(1l)), neg(plus(pos(number(2l)), pos(number(3l))))),
				predicate
		);
	}
	
	@Test(expected = SyntaxErrorException.class)
	public void testAdditionParenthesisDoubleNegated() {
		PredicateCompiler.parsePredicate("1>--(2+3))");
	}
	
	@Test
	public void testAdditionParenthesisDoubleNegatedParanthesis() {
		Predicate predicate = PredicateCompiler.parsePredicate("1>-(-(2+3))");
		assertEquals(
				gt(pos(number(1l)), neg(neg(plus(pos(number(2l)), pos(number(3l)))))),
				predicate
		);
	}
	
	@Test
	public void testBetween() {
		Predicate predicate = PredicateCompiler.parsePredicate("1 BETWEEN 3 AND 5");
		assertEquals(
				between(pos(number(1l)), pos(number(3l)), pos(number(5))),
				predicate
		);
	}
	
	@Test
	public void testAttributeBetween() {
		Predicate predicate = PredicateCompiler.parsePredicate("user.id BETWEEN 3 AND 5");
		assertEquals(
				between(pos(arithmeticAttr("user.id")), pos(number(3l)), pos(number(5))),
				predicate
		);
	}
	
	@Test
	public void testNumericAttribute() {
		Predicate predicate = PredicateCompiler.parsePredicate("1 BETWEEN user.age AND 5");
		assertEquals(
				between(pos(number(1l)), pos(arithmeticAttr("user.age")), pos(number(5))),
				predicate
		);
	}
	
	@Test
	public void testDateTime1() {
		Predicate predicate = PredicateCompiler.parsePredicate("user.birthday BETWEEN TIMESTAMP('2015-11-19 15:00:00') AND CURRENT_TIMESTAMP");
		assertEquals(
				between(dateTimeAttr("user.birthday"), time("2015-11-19 15:00:00"), now()),
				predicate
		);
	}
	
	@Test
	public void testDateTime2() {
		Predicate predicate = PredicateCompiler.parsePredicate("user.birthday BETWEEN TIMESTAMP('2015-11-20') AND CURRENT_TIMESTAMP AND user.registrationDate < TIMESTAMP('2015-11-19 15:00:00')");
		assertEquals(
				and(
						between(dateTimeAttr("user.birthday"), time("2015-11-20"), now()),
						lt(dateTimeAttr("user.registrationDate"), time("2015-11-19 15:00:00"))
				),
				predicate
		);
	}
	
	@Test
	public void testEnum1() {
		Predicate predicate = PredicateCompiler.parsePredicate("user.gender = ENUM_VALUE('Gender','MALE')");
		assertEquals(
				eq(enumAttr("user.gender"), enumValue("Gender", "MALE")),
				predicate
		);
	}
	
	@Test
	public void testStringComparison() {
		Predicate predicate = PredicateCompiler.parsePredicate("'test' != user.email");
		assertEquals(
				neq(string("test"), stringAttr("user.email")),
				predicate
		);
	}
	
	@Test
	public void testStringInCollection() {
		Predicate predicate = PredicateCompiler.parsePredicate("'test' IN (user.styles)");
		assertEquals(
				inCollection(string("test"), collectionAttr("user.styles")),
				predicate
		);
	}
	
	@Test
	public void testStringInLiterals() {
		Predicate predicate = PredicateCompiler.parsePredicate("'test' IN ('a','b','test')");
		assertEquals(
				in(string("test"), string("a"), string("b"), string("test")),
				predicate
		);
	}
	
	@Test
	public void testAttributeInLiterals() {
		Predicate predicate = PredicateCompiler.parsePredicate("user.age IN (1)");
		assertEquals(
				in(pos(arithmeticAttr("user.age")), pos(number(1))),
				predicate
		);
	}
	
	@Test
	public void testArithmeticInCollection() {
		Predicate predicate = PredicateCompiler.parsePredicate("1 + user.age IN (user.styles)");
		assertEquals(
				inCollection(plus(pos(number(1)), pos(arithmeticAttr("user.age"))), collectionAttr("user.styles")),
				predicate
		);
	}
	
	@Test
	public void testArithmeticInLiterals() {
		Predicate predicate = PredicateCompiler.parsePredicate("1 + user.age IN (1,2,-3,4)");
		assertEquals(
				in(plus(pos(number(1)), pos(arithmeticAttr("user.age"))), pos(number(1)), pos(number(2)), neg(number(3)), pos(number(4))),
				predicate
		);
	}
	
	@Test
	public void testEnumInLiterals() {
		Predicate predicate = PredicateCompiler.parsePredicate("user.gender IN (ENUM_VALUE('Gender','MALE'),ENUM_VALUE('Gender','FEMALE'))");
		assertEquals(
					in(enumAttr("user.gender"), enumValue("Gender", "MALE"), enumValue("Gender", "FEMALE")),
				predicate
		);
	}
	
	@Test
	public void testBooleanExpression() {
		Predicate predicate = PredicateCompiler.parsePredicate("(1 < 2 AND 2 >= 4) OR ('test' = user.email AND 'A' != 'B')");
		assertEquals(
				or(
					and(
							lt(pos(number(1)), pos(number(2))), 
							ge(pos(number(2)), pos(number(4)))
					),
					and(
							eq(string("test"), stringAttr("user.email")),
							neq(string("A"), string("B"))
					)
				),
				predicate
		);
	}
	
}
