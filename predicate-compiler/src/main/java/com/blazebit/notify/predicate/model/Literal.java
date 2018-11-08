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

import com.blazebit.notify.predicate.parser.SyntaxErrorException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Literal<T> {
	
	private static final Pattern DATE_TIME_EXTRACTION_PATTERN = Pattern.compile("TIMESTAMP\\('([^\\)]+)'\\)");
	private static final ThreadLocal<DateFormat> DATE_LITERAL_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};
	private static final ThreadLocal<DateFormat> DATE_TIME_LITERAL_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};
	private static final  ThreadLocal<DateFormat> DATE_TIME_MILLISECONDS_LITERAL_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		}
	};
	private static final Map<String, Class<? extends Enum<?>>> ENUM_CLASSES = new HashMap<String, Class<? extends Enum<?>>>();
	private static final Map<Class<? extends Enum<?>>, String> ENUM_NAMES = new HashMap<Class<? extends Enum<?>>, String>();
	
	static {
//		ENUM_CLASSES.put("Gender", UserGender.class);
//		ENUM_CLASSES.put("Device", UserDeviceType.class);
		
		for (Map.Entry<String, Class<? extends Enum<?>>> entry : ENUM_CLASSES.entrySet()) {
			ENUM_NAMES.put(entry.getValue(), entry.getKey());
		}
	}
	
	private final T value;

	private Literal(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Literal<?> other = (Literal<?>) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public static <T> Literal<T> of(T value) {
		return new Literal<T>(value);
	}
	
	public static Literal<Enum<?>> ofEnumLiteral(String enumLiteral) {
		int dotIndex = enumLiteral.indexOf('.');
		Class<? extends Enum<?>> enumClass = getEnumClass(enumLiteral, dotIndex);
		String enumKey = enumLiteral.substring(dotIndex + 1);

		@SuppressWarnings("unchecked")
		Literal<Enum<?>> literal = (Literal<Enum<?>>) (Literal) new Literal<>(getEnumValue(enumClass, enumKey));
		return literal;
	}

	public static Literal<List<Enum<?>>> ofEnumCollectionLiteral(String enumLiteral) {
		int dotIndex = enumLiteral.indexOf('.');
		Class<? extends Enum<?>> enumClass = getEnumClass(enumLiteral, dotIndex);
		int startIndex = enumLiteral.indexOf('(', dotIndex) + 1;
		
		if (startIndex == 0 || enumLiteral.charAt(enumLiteral.length() - 1) != ')') {
			throw new SyntaxErrorException("Invalid input for enum collection literal [" + enumLiteral + "]!");
		}
		
		List<Enum<?>> enumValues = new ArrayList<Enum<?>>();

		int endIndex;
		
		while ((endIndex = enumLiteral.indexOf(',', startIndex)) != -1) {
			enumValues.add(getEnumValue(enumClass, getEnumCollectionItem(enumLiteral, startIndex, endIndex)));
			startIndex = endIndex + 1;
		}
		
		enumValues.add(getEnumValue(enumClass, getEnumCollectionItem(enumLiteral, startIndex, enumLiteral.length() - 1)));
		
		return new Literal<>(enumValues);
	}
	
	public static String getEnumNameOf(Class<? extends Enum<?>> clazz) {
		return ENUM_NAMES.get(clazz);
	}
	
	private static String getEnumCollectionItem(String enumLiteral, int startIndex, int endIndex) {
		while (Character.isWhitespace(enumLiteral.charAt(startIndex))) {
			startIndex++;
		}
		while (Character.isWhitespace(enumLiteral.charAt(endIndex))) {
			endIndex--;
		}
		return enumLiteral.substring(startIndex, endIndex).trim();
	}

	private static Enum<?> getEnumValue(Class<? extends Enum<?>> enumClass, String enumKey) {
		try {
		    @SuppressWarnings("unchecked")
		    Enum<?> enumValue = Enum.valueOf((Class<Enum>) (Class) enumClass, enumKey);
		    return enumValue;
        } catch(IllegalStateException e) {
		    throw new SyntaxErrorException("Then enum class [" + enumClass.getName() + "] has no enum key named [" + enumKey + "]");
        }
	}
	
	private static Class<? extends Enum<?>> getEnumClass(String enumLiteral, int dotIndex) {
		String enumName = enumLiteral.substring(0, dotIndex);
		Class<? extends Enum<?>> enumClass = ENUM_CLASSES.get(enumName);
		
		if (enumClass == null) {
			throw new SyntaxErrorException("There is no enum class registered for the name [" + enumName + "]!");
		}
		
		return enumClass;
	}
	
	public static Literal<String> ofQuotedString(String quotedString) {
		if (quotedString.length() >= 2 && quotedString.charAt(0) == '\'' && quotedString.charAt(quotedString.length() - 1) == '\'') {
			return new Literal<>(quotedString.substring(1, quotedString.length() - 1));
		} else {
			throw new SyntaxErrorException("String not quoted [" + quotedString + "]");
		}
	}
	
	public static Literal<Calendar> ofDateTimeString(String dateTimeString) {
		Matcher dateTimeMatcher = DATE_TIME_EXTRACTION_PATTERN.matcher(dateTimeString);
		if (dateTimeMatcher.find()) {
			String extractedDateTimeString = dateTimeMatcher.group(1);
			
			boolean hasBlank = extractedDateTimeString.contains(" ");
			boolean hasDot = extractedDateTimeString.contains(".");
			
			try {
				Calendar dateTime;
				if (!hasBlank && !hasDot) {
					dateTime = Calendar.getInstance();
					dateTime.setTime(DATE_LITERAL_FORMAT.get().parse(extractedDateTimeString));
				} else if (!hasDot) {
					dateTime = Calendar.getInstance();
					dateTime.setTime(DATE_TIME_LITERAL_FORMAT.get().parse(extractedDateTimeString));
				} else {
					dateTime = Calendar.getInstance();
					dateTime.setTime(DATE_TIME_MILLISECONDS_LITERAL_FORMAT.get().parse(extractedDateTimeString));
				}
				return new Literal<>(dateTime);
			} catch (ParseException e) {
				throw new SyntaxErrorException("Invalid datetime literal " + dateTimeString);
			}
		} else {
			throw new SyntaxErrorException("Invalid datetime literal " + dateTimeString);
		}
	}
	
	public static Literal<BigDecimal> ofNumericString(String numericString) {
		try {
			return new Literal<>(new BigDecimal(numericString));
		} catch (NumberFormatException e) {
			throw new SyntaxErrorException(e);
		}
	}
	
}
