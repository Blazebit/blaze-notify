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
		public void visit(ArithmeticAtom e);
		public void visit(ArithmeticFactor e);
		public void visit(ArithmeticBetweenPredicate e);
		public void visit(ArithmeticInCollectionPredicate e);
		public void visit(ArithmeticInPredicate e);
		public void visit(ChainingArithmeticExpression e);
		public void visit(CollectionAtom e);
		public void visit(ConjunctivePredicate e);
		public void visit(DisjunctivePredicate e);
		public void visit(DateTimeAtom e);
		public void visit(DateTimeBetweenPredicate e);
		public void visit(ComparisonPredicate e);
		public void visit(IsNullPredicate e);
		public void visit(StringAtom e);
		public void visit(StringInCollectionPredicate e);
		public void visit(StringInPredicate e);
		public void visit(EnumAtom e);
		public void visit(EnumInCollectionPredicate e);
		public void visit(EnumInPredicate e);
		public void visit(Attribute e);
	}

	public static interface ResultVisitor<T> {
		public T visit(ArithmeticAtom e);
		public T visit(ArithmeticFactor e);
		public T visit(ArithmeticBetweenPredicate e);
		public T visit(ArithmeticInCollectionPredicate e);
		public T visit(ArithmeticInPredicate e);
		public T visit(ChainingArithmeticExpression e);
		public T visit(CollectionAtom e);
		public T visit(ConjunctivePredicate e);
		public T visit(DisjunctivePredicate e);
		public T visit(DateTimeAtom e);
		public T visit(DateTimeBetweenPredicate e);
		public T visit(ComparisonPredicate e);
		public T visit(IsNullPredicate e);
		public T visit(StringAtom e);
		public T visit(StringInCollectionPredicate e);
		public T visit(StringInPredicate e);
		public T visit(EnumAtom e);
		public T visit(EnumInCollectionPredicate e);
		public T visit(EnumInPredicate e);
		public T visit(Attribute e);
	}

	public void accept(Visitor visitor);

    public <T> T accept(ResultVisitor<T> visitor);
}
