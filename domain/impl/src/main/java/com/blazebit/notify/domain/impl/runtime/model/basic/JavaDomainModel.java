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

package com.blazebit.notify.domain.impl.runtime.model.basic;

import com.blazebit.notify.domain.impl.runtime.model.BasicDomainTypeImpl;
import com.blazebit.notify.domain.runtime.model.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.EnumSet;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class JavaDomainModel {

    public static final BasicDomainType INTEGER = new BasicDomainTypeImpl(Integer.class, DomainOperator.arithmetic(), DomainPredicateType.comparable());
    public static final BasicDomainType DECIMAL = new BasicDomainTypeImpl(BigDecimal.class, DomainOperator.arithmetic(), DomainPredicateType.comparable());
    public static final BasicDomainType STRING = new BasicDomainTypeImpl(String.class, EnumSet.of(DomainOperator.PLUS), DomainPredicateType.comparable());
    public static final BasicDomainType CALENDAR = new BasicDomainTypeImpl(Calendar.class, EnumSet.of(DomainOperator.PLUS, DomainOperator.MINUS), DomainPredicateType.comparable());
    public static final BasicDomainType BOOLEAN = new BasicDomainTypeImpl(Boolean.class, EnumSet.of(DomainOperator.NOT), DomainPredicateType.equality());

    public static final BooleanLiteralTypeResolver BOOLEAN_LITERAL_TYPE_RESOLVER = new BooleanLiteralTypeResolver() {
        @Override
        public ResolvedLiteral resolveLiteral(DomainModel domainModel, Boolean value) {
            return new ResolvedLiteralImpl(domainModel.getType(Boolean.class), value);
        }
    };
    public static final NumericLiteralTypeResolver NUMERIC_LITERAL_TYPE_RESOLVER = new NumericLiteralTypeResolver() {
        @Override
        public ResolvedLiteral resolveLiteral(DomainModel domainModel, Number value) {
            return new ResolvedLiteralImpl(domainModel.getType(BigDecimal.class), value);
        }
    };
    public static final TemporalLiteralTypeResolver TEMPORAL_LITERAL_TYPE_RESOLVER = new TemporalLiteralTypeResolver() {
        @Override
        public ResolvedLiteral resolveLiteral(DomainModel domainModel, Calendar value) {
            return new ResolvedLiteralImpl(domainModel.getType(Calendar.class), value);
        }
    };
    public static final StringLiteralTypeResolver STRING_LITERAL_TYPE_RESOLVER = new StringLiteralTypeResolver() {
        @Override
        public ResolvedLiteral resolveLiteral(DomainModel domainModel, String value) {
            return new ResolvedLiteralImpl(domainModel.getType(String.class), value);
        }
    };

}
