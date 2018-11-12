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
public class StringPredicateCompilerTest extends AbstractPredicateCompilerTest {
	
	private final String expr;
	private final Expression expectedExpression;
	
	public StringPredicateCompilerTest(String expr, Expression expectedExpression) {
		this.expr = expr;
		this.expectedExpression = expectedExpression;
	}
	
	@Parameters(name = "{1} {2}")
	public static Collection<Object[]> getTestData() {
		Object[][] literals = {
				{ "''", string("") },
				{ "'abc'", string("abc") }
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
		assertEquals(expectedExpression, parseStringExpression(expr));
	}
}
