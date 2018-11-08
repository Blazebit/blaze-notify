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

public interface Predicate {

	public static interface Visitor {
		public void visit(SimplePredicate e);
		public void visit(ArithmeticAtom e);
		public void visit(ArithmeticFactor e);
		public void visit(ArithmeticBetweenPredicate e);
		public void visit(ArithmeticInAttributePredicate e);
		public void visit(ArithmeticInLiteralPredicate e);
		public void visit(ChainingArithmeticExpression e);
		public void visit(CollectionAtom e);
		public void visit(ConjunctivePredicate e);
		public void visit(DisjunctivePredicate e);
		public void visit(DateTimeAtom e);
		public void visit(DateTimeBetweenPredicate e);
		public void visit(SimpleComparisonPredicate e);
		public void visit(IsNullPredicate e);
		public void visit(StringAtom e);
		public void visit(StringInAttributePredicate e);
		public void visit(StringInLiteralPredicate e);
		public void visit(EnumAtom e);
		public void visit(EnumCollectionAtom e);
		public void visit(EnumInLiteralPredicate e);
	}
	
	public static interface ResultVisitor<T> {
		public T visit(SimplePredicate e);
		public T visit(ArithmeticAtom e);
		public T visit(ArithmeticFactor e);
		public T visit(ArithmeticBetweenPredicate e);
		public T visit(ArithmeticInAttributePredicate e);
		public T visit(ArithmeticInLiteralPredicate e);
		public T visit(ChainingArithmeticExpression e);
		public T visit(CollectionAtom e);
		public T visit(ConjunctivePredicate e);
		public T visit(DisjunctivePredicate e);
		public T visit(DateTimeAtom e);
		public T visit(DateTimeBetweenPredicate e);
		public T visit(SimpleComparisonPredicate e);
		public T visit(IsNullPredicate e);
		public T visit(StringAtom e);
		public T visit(StringInAttributePredicate e);
		public T visit(StringInLiteralPredicate e);
		public T visit(EnumAtom e);
		public T visit(EnumCollectionAtom e);
		public T visit(EnumInLiteralPredicate e);
	}
	
	public void accept(Visitor visitor);

    public <T> T accept(ResultVisitor<T> visitor);

    
}
