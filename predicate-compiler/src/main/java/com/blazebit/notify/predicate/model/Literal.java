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
import com.blazebit.notify.predicate.parser.SyntaxErrorException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Literal<T> {
	
	private final T value;
	private final DomainType type;

	private Literal(T value, DomainType type) {
		this.value = value;
		this.type = type;
	}

	public T getValue() {
		return value;
	}

	public DomainType getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Literal<?> literal = (Literal<?>) o;
		return Objects.equals(value, literal.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	public static <T> Literal<T> of(T value, DomainType type) {
		return new Literal<T>(value, type);
	}
}
