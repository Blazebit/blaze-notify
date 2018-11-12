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

import com.blazebit.notify.domain.runtime.model.DomainType;

import java.util.Objects;

public class Attribute {
	private final String identifier;
	private final DomainType type;

	public Attribute(String identifier, DomainType type) {
		this.identifier = identifier;
		this.type = type;
	}

	public String getIdentifier() {
		return identifier;
	}

	public DomainType getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Attribute attribute = (Attribute) o;
		return Objects.equals(identifier, attribute.identifier) &&
				type == attribute.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, type);
	}
}
