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
package com.blazebit.notify.expression.impl;

import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.expression.EnumValue;
import com.blazebit.notify.expression.Literal;
import com.blazebit.notify.expression.SyntaxErrorException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiteralFactory {

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
    private static final ThreadLocal<DateFormat> DATE_TIME_MILLISECONDS_LITERAL_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
    };

    private final DomainModel domainModel;
    private Literal<Calendar> currentTimestampLiteral;

    public LiteralFactory(DomainModel domainModel) {
        this.domainModel = domainModel;
    }

    public Literal<EnumValue> ofEnumValue(EnumValue enumValue) {
        return Literal.of(enumValue, domainModel.getType(enumValue.getEnumName()));
    }

    public Literal<Calendar> currentTimestamp() {
        if (currentTimestampLiteral == null) {
            currentTimestampLiteral = Literal.of(null, domainModel.getType(Calendar.class));
        }
        return currentTimestampLiteral;
    }

    public Literal<String> ofQuotedString(String quotedString) {
        if (quotedString.length() >= 2 && quotedString.charAt(0) == '\'' && quotedString.charAt(quotedString.length() - 1) == '\'') {
            return ofString(quotedString.substring(1, quotedString.length() - 1));
        } else {
            throw new SyntaxErrorException("String not quoted [" + quotedString + "]");
        }
    }

    public Literal<String> ofString(String string) {
        return Literal.of(string, domainModel.getType(String.class));
    }

    public Literal<Calendar> ofDateTimeString(String dateTimeString) {
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
                return ofCalendar(dateTime);
            } catch (ParseException e) {
                throw new SyntaxErrorException("Invalid datetime literal " + dateTimeString);
            }
        } else {
            throw new SyntaxErrorException("Invalid datetime literal " + dateTimeString);
        }
    }

    public Literal<Calendar> ofCalendar(Calendar calendar) {
        return Literal.of(calendar, domainModel.getType(Calendar.class));
    }

    public Literal<BigDecimal> ofNumericString(String numericString) {
        try {
            return ofBigDecimal(new BigDecimal(numericString));
        } catch (NumberFormatException e) {
            throw new SyntaxErrorException(e);
        }
    }

    public Literal<BigDecimal> ofBigDecimal(BigDecimal bigDecimal) {
        return Literal.of(bigDecimal, domainModel.getType(BigDecimal.class));
    }
}
