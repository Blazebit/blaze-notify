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

import com.blazebit.notify.domain.runtime.model.*;
import com.blazebit.notify.expression.DomainModelException;
import com.blazebit.notify.expression.EnumValue;
import com.blazebit.notify.expression.SyntaxErrorException;
import com.blazebit.notify.expression.TypeErrorException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LiteralFactory {

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

    private static final String TEMPORAL_INTERVAL_YEARS_FIELD = "years";
    private static final String TEMPORAL_INTERVAL_MONTHS_FIELD = "months";
    private static final String TEMPORAL_INTERVAL_DAYS_FIELD = "days";
    private static final String TEMPORAL_INTERVAL_HOURS_FIELD = "hours";
    private static final String TEMPORAL_INTERVAL_MINUTES_FIELD = "minutes";
    private static final String TEMPORAL_INTERVAL_SECONDS_FIELD = "seconds";

    private final DomainModel domainModel;
    private final NumericLiteralTypeResolver numericLiteralTypeResolver;
    private final BooleanLiteralTypeResolver booleanLiteralTypeResolver;
    private final StringLiteralTypeResolver stringLiteralTypeResolver;
    private final TemporalLiteralTypeResolver temporalLiteralTypeResolver;
    private final EnumLiteralTypeResolver enumLiteralTypeResolver;

    public LiteralFactory(DomainModel domainModel) {
        this.domainModel = domainModel;
        this.numericLiteralTypeResolver = domainModel.getNumericLiteralTypeResolver();
        this.booleanLiteralTypeResolver = domainModel.getBooleanLiteralTypeResolver();
        this.stringLiteralTypeResolver = domainModel.getStringLiteralTypeResolver();
        this.temporalLiteralTypeResolver = domainModel.getTemporalLiteralTypeResolver();
        this.enumLiteralTypeResolver = domainModel.getEnumLiteralTypeResolver();
    }

    public ResolvedLiteral ofEnumValue(EnumValue enumValue) {
        DomainType domainType = domainModel.getType(enumValue.getEnumName());
        if (domainType == null) {
            throw new DomainModelException(String.format("Undefined enum type '%s'", enumValue.getEnumName()));
        } else if (domainType.getKind() == DomainType.DomainTypeKind.ENUM) {
            EnumDomainTypeValue domainEnumValue = ((EnumDomainType) domainType).getEnumValues().get(enumValue.getEnumKey());
            if (enumLiteralTypeResolver == null) {
                throw new DomainModelException("No literal type resolver for enum literals defined");
            }
            return enumLiteralTypeResolver.resolveLiteral(domainModel, domainEnumValue);
        } else {
            throw new TypeErrorException("Expected domain type of kind " + DomainType.DomainTypeKind.ENUM + " for type name " + enumValue.getEnumName());
        }
    }

    public ResolvedLiteral ofTemporalIntervalString(String intervalString) {
        String[] intervalStringParts = intervalString.split("\\s");
        int years = 0, months = 0, days = 0, hours = 0, minutes = 0, seconds = 0;
        for (int i = 0; i < intervalStringParts.length / 2; i++) {
            int amount = Integer.parseInt(intervalStringParts[2 * i]);
            String temporalField = intervalStringParts[2 * i + 1];

            if (TEMPORAL_INTERVAL_YEARS_FIELD.equalsIgnoreCase(temporalField)) {
                years = amount;
            } else if (TEMPORAL_INTERVAL_MONTHS_FIELD.equalsIgnoreCase(temporalField)) {
                months = amount;
            } else if (TEMPORAL_INTERVAL_DAYS_FIELD.equalsIgnoreCase(temporalField)) {
                days = amount;
            } else if (TEMPORAL_INTERVAL_HOURS_FIELD.equalsIgnoreCase(temporalField)) {
                hours = amount;
            } else if (TEMPORAL_INTERVAL_MINUTES_FIELD.equalsIgnoreCase(temporalField)) {
                minutes = amount;
            } else if (TEMPORAL_INTERVAL_SECONDS_FIELD.equalsIgnoreCase(temporalField)) {
                seconds = amount;
            }
        }

        TemporalInterval interval = new TemporalInterval(years, months, days, hours, minutes, seconds);
        if (temporalLiteralTypeResolver == null) {
            throw new DomainModelException("No literal type resolver for temporal interval literals defined");
        }
        return temporalLiteralTypeResolver.resolveIntervalLiteral(domainModel, interval);
    }

    public ResolvedLiteral ofQuotedString(String quotedString) {
        if (quotedString.length() >= 2 && quotedString.charAt(0) == '\'' && quotedString.charAt(quotedString.length() - 1) == '\'') {
            return ofString(quotedString.substring(1, quotedString.length() - 1));
        } else {
            throw new SyntaxErrorException("String not quoted [" + quotedString + "]");
        }
    }

    public ResolvedLiteral ofString(String string) {
        if (stringLiteralTypeResolver == null) {
            throw new DomainModelException("No literal type resolver for string literals defined");
        }
        return stringLiteralTypeResolver.resolveLiteral(domainModel, string);
    }

    public ResolvedLiteral ofDateTimeString(String dateTimeString) {
        boolean hasBlank = dateTimeString.contains(" ");
        boolean hasDot = dateTimeString.contains(".");

        try {
            Calendar dateTime;
            if (!hasBlank && !hasDot) {
                dateTime = Calendar.getInstance();
                dateTime.setTime(DATE_LITERAL_FORMAT.get().parse(dateTimeString));
            } else if (!hasDot) {
                dateTime = Calendar.getInstance();
                dateTime.setTime(DATE_TIME_LITERAL_FORMAT.get().parse(dateTimeString));
            } else {
                dateTime = Calendar.getInstance();
                dateTime.setTime(DATE_TIME_MILLISECONDS_LITERAL_FORMAT.get().parse(dateTimeString));
            }
            return ofCalendar(dateTime);
        } catch (ParseException e) {
            throw new SyntaxErrorException("Invalid datetime literal " + dateTimeString);
        }
    }

    public ResolvedLiteral ofCalendar(Calendar calendar) {
        if (temporalLiteralTypeResolver == null) {
            throw new DomainModelException("No literal type resolver for temporal literals defined");
        }
        return temporalLiteralTypeResolver.resolveTimestampLiteral(domainModel, calendar);
    }

    public ResolvedLiteral ofNumericString(String numericString) {
        try {
            return ofBigDecimal(new BigDecimal(numericString));
        } catch (NumberFormatException e) {
            throw new SyntaxErrorException(e);
        }
    }

    public ResolvedLiteral ofBigDecimal(BigDecimal bigDecimal) {
        if (numericLiteralTypeResolver == null) {
            throw new DomainModelException("No literal type resolver for numeric literals defined");
        }
        return numericLiteralTypeResolver.resolveLiteral(domainModel, bigDecimal);
    }

    public ResolvedLiteral ofBoolean(Boolean value) {
        if (booleanLiteralTypeResolver == null) {
            throw new DomainModelException("No literal type resolver for boolean literals defined");
        }
        return booleanLiteralTypeResolver.resolveLiteral(domainModel, value);
    }
}
