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


import java.util.Objects;

public abstract class BetweenPredicate<T extends TermExpression> extends AbstractPredicate {
	private final T left;
	private final T upper;
	private final T lower;

	public BetweenPredicate(T left, T upper, T lower) {
		super(false);
		this.left = left;
		this.upper = upper;
		this.lower = lower;
	}

	public T getLeft() {
		return left;
	}

	public T getUpper() {
		return upper;
	}

	public T getLower() {
		return lower;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		BetweenPredicate<?> that = (BetweenPredicate<?>) o;
		return Objects.equals(left, that.left) &&
				Objects.equals(upper, that.upper) &&
				Objects.equals(lower, that.lower);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), left, upper, lower);
	}
}
