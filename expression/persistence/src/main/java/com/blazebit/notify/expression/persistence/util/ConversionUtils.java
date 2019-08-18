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

package com.blazebit.notify.expression.persistence.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class ConversionUtils {

    private ConversionUtils() {
    }

    public static BigDecimal getBigDecimal(Object v) {
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        } else if (v instanceof BigInteger) {
            return new BigDecimal((BigInteger) v);
        } else if (v instanceof Long) {
            return new BigDecimal((Long) v);
        } else if (v instanceof Integer) {
            return new BigDecimal((Integer) v);
        } else if (v instanceof Double) {
            return new BigDecimal((Double) v);
        } else if (v instanceof Float) {
            return new BigDecimal((Float) v);
        } else if (v instanceof Number) {
            return new BigDecimal(((Number) v).doubleValue());
        }

        throw new IllegalArgumentException("Can't widen to BigDecimal: " + v);
    }

    public static BigInteger getBigInteger(Object v) {
        if (v instanceof BigInteger) {
            return (BigInteger) v;
        } else if (v instanceof Long) {
            return BigInteger.valueOf((Long) v);
        } else if (v instanceof Integer) {
            return BigInteger.valueOf((Integer) v);
        } else if (v instanceof Short) {
            return BigInteger.valueOf((Short) v);
        } else if (v instanceof Byte) {
            return BigInteger.valueOf((Byte) v);
        }

        throw new IllegalArgumentException("Can't widen to BigInteger: " + v);
    }
}
