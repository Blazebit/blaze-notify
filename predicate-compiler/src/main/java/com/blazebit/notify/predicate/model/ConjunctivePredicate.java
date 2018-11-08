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



public class ConjunctivePredicate implements Predicate {
	private final List<Predicate> conjuncts;
	private final boolean negated;
	
	public ConjunctivePredicate(List<Predicate> conjuncts) {
		this(conjuncts, false);
	}
	
	public ConjunctivePredicate(List<Predicate> conjuncts, boolean negated) {
		this.conjuncts = conjuncts;
		this.negated = negated;
	}

	public List<Predicate> getConjuncts() {
		return conjuncts;
	}

	public boolean isNegated() {
		return negated;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conjuncts == null) ? 0 : conjuncts.hashCode());
		result = prime * result + (negated ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConjunctivePredicate other = (ConjunctivePredicate) obj;
		if (conjuncts == null) {
			if (other.conjuncts != null)
				return false;
		} else if (!conjuncts.equals(other.conjuncts))
			return false;
		if (negated != other.negated)
			return false;
		return true;
	}

}
