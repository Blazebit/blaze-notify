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

import com.blazebit.notify.predicate.model.Expression;
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
	private final Expression expectedExpression;
	
	public DatetimePredicateCompilerTest(String expr, Expression expectedExpression) {
		this.expr = expr;
		this.expectedExpression = expectedExpression;
	}
	
	@Parameters(name = "{1} {2}")
	public static Collection<Object[]> getTestData() {
		Object[][] literals = {
				{ "CURRENT_TIMESTAMP", now() },
				{ "TIMESTAMP('2014-01-01')", time("2014-01-01") },
				{ "TIMESTAMP('2014-01-01 00:00:00')", time("2014-01-01 00:00:00") },
				{ "TIMESTAMP('2014-01-01 00:00:00.000')", time("2014-01-01 00:00:00.000") },
				{ "TIMESTAMP('2014-01-01 01:01:01.100')", time("2014-01-01 01:01:01.100") }
		};
		
		List<Object[]> parameters = new ArrayList<>(literals.length);
		for (int opIdx = 0; opIdx < literals.length; opIdx++) {
			parameters.add(new Object[] {
					literals[opIdx][0],
					literals[opIdx][1]
			});
		}
		
		return parameters;
	}
	
	@Test
	public void comparisonWithLiteralTest() {
		assertEquals(expectedExpression, parseDateTimeExpression(expr));
	}
}
