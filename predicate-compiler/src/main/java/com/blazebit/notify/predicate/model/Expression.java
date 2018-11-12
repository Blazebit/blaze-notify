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

package com.blazebit.notify.predicate.model;

import org.w3c.dom.Attr;

public interface Expression {
    public static interface Visitor {
		public void visit(ArithmeticFactor e);
		public void visit(BetweenPredicate e);
		public void visit(InPredicate e);
		public void visit(ChainingArithmeticExpression e);
		public void visit(ConjunctivePredicate e);
		public void visit(DisjunctivePredicate e);
		public void visit(Atom e);
		public void visit(ComparisonPredicate e);
		public void visit(IsNullPredicate e);
		public void visit(Attribute e);
	}

	public static interface ResultVisitor<T> {
		public T visit(ArithmeticFactor e);
		public T visit(BetweenPredicate e);
		public T visit(InPredicate e);
		public T visit(ChainingArithmeticExpression e);
		public T visit(ConjunctivePredicate e);
		public T visit(DisjunctivePredicate e);
		public T visit(Atom e);
		public T visit(ComparisonPredicate e);
		public T visit(IsNullPredicate e);
		public T visit(Attribute e);
	}

	public void accept(Visitor visitor);

    public <T> T accept(ResultVisitor<T> visitor);
}
