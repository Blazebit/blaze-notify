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

import java.util.List;
import java.util.Objects;


public class ConjunctivePredicate extends AbstractPredicate {
	private final List<Predicate> conjuncts;

	public ConjunctivePredicate(List<Predicate> conjuncts) {
		this(conjuncts, false);
	}
	
	public ConjunctivePredicate(List<Predicate> conjuncts, boolean negated) {
		super(negated);
		this.conjuncts = conjuncts;
	}

	public List<Predicate> getConjuncts() {
		return conjuncts;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(ResultVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		ConjunctivePredicate that = (ConjunctivePredicate) o;
		return Objects.equals(conjuncts, that.conjuncts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), conjuncts);
	}
}
