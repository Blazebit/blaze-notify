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

import java.util.Calendar;
import java.util.Objects;

public class DateTimeAtom extends AbstractAtom<Calendar> {
	private final boolean currentTimestamp;
	
	public DateTimeAtom(Attribute attribute) {
		super(attribute);
		this.currentTimestamp = false;
	}

	public DateTimeAtom(Literal<Calendar> literal) {
		super(literal);
		this.currentTimestamp = false;
	}

	public DateTimeAtom(boolean currentTimestamp) {
		super((Attribute) null);
		this.currentTimestamp = currentTimestamp;
	}

	public boolean isCurrentTimestamp() {
		return currentTimestamp;
	}

	@Override
	public TermType getType() {
		return TermType.DATE_TIME;
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
		DateTimeAtom that = (DateTimeAtom) o;
		return currentTimestamp == that.currentTimestamp;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), currentTimestamp);
	}
}
