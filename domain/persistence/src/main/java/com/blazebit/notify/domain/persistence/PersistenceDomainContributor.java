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

package com.blazebit.notify.domain.persistence;

import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.runtime.model.*;
import com.blazebit.notify.domain.spi.DomainContributor;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PersistenceDomainContributor implements DomainContributor {

    private static final BooleanLiteralResolver BOOLEAN_LITERAL_TYPE_RESOLVER = new BooleanLiteralResolver() {
        @Override
        public ResolvedLiteral resolveLiteral(DomainModel domainModel, Boolean value) {
            return new DefaultResolvedLiteral(domainModel.getType(Boolean.class), value);
        }
    };
    private static final NumericLiteralResolver NUMERIC_LITERAL_TYPE_RESOLVER = new NumericLiteralResolver() {
        @Override
        public ResolvedLiteral resolveLiteral(DomainModel domainModel, Number value) {
            return new DefaultResolvedLiteral(domainModel.getType(BigDecimal.class), value);
        }
    };
    private static final TemporalLiteralResolver TEMPORAL_LITERAL_TYPE_RESOLVER = new TemporalLiteralResolver() {
        @Override
        public ResolvedLiteral resolveTimestampLiteral(DomainModel domainModel, Calendar value) {
            return new DefaultResolvedLiteral(domainModel.getType(Calendar.class), value);
        }

        @Override
        public ResolvedLiteral resolveIntervalLiteral(DomainModel domainModel, TemporalInterval value) {
            return new DefaultResolvedLiteral(domainModel.getType(TemporalInterval.class), value);
        }
    };
    private static final StringLiteralResolver STRING_LITERAL_TYPE_RESOLVER = new StringLiteralResolver() {
        @Override
        public ResolvedLiteral resolveLiteral(DomainModel domainModel, String value) {
            return new DefaultResolvedLiteral(domainModel.getType(String.class), value);
        }
    };

    @Override
    public void contribute(DomainBuilder domainBuilder) {
        createBasicType(domainBuilder, Integer.class, DomainOperator.arithmetic(), DomainPredicateType.comparable());
        createBasicType(domainBuilder, BigDecimal.class, DomainOperator.arithmetic(), DomainPredicateType.comparable());
        createBasicType(domainBuilder, String.class, new DomainOperator[]{ DomainOperator.PLUS }, DomainPredicateType.comparable());
        createBasicType(domainBuilder, Calendar.class, new DomainOperator[]{ DomainOperator.PLUS, DomainOperator.MINUS }, DomainPredicateType.comparable());
        createBasicType(domainBuilder, Boolean.class, new DomainOperator[]{ DomainOperator.NOT }, DomainPredicateType.distinguishable());
        domainBuilder.withLiteralTypeResolver(NUMERIC_LITERAL_TYPE_RESOLVER);
        domainBuilder.withLiteralTypeResolver(STRING_LITERAL_TYPE_RESOLVER);
        domainBuilder.withLiteralTypeResolver(TEMPORAL_LITERAL_TYPE_RESOLVER);
        domainBuilder.withLiteralTypeResolver(BOOLEAN_LITERAL_TYPE_RESOLVER);

        for (Class<?> type : Arrays.asList(Integer.class, BigDecimal.class)) {
            domainBuilder.withOperationTypeResolver(type, DomainOperator.MODULO, StaticDomainOperationTypeResolvers.returning(Integer.class));
            domainBuilder.withOperationTypeResolver(type, DomainOperator.UNARY_MINUS, StaticDomainOperationTypeResolvers.returning(type));
            domainBuilder.withOperationTypeResolver(type, DomainOperator.UNARY_PLUS, StaticDomainOperationTypeResolvers.returning(type));
            domainBuilder.withOperationTypeResolver(type, DomainOperator.DIVISION, StaticDomainOperationTypeResolvers.returning(BigDecimal.class));
            for (DomainOperator domainOperator : Arrays.asList(DomainOperator.PLUS, DomainOperator.MINUS, DomainOperator.MULTIPLICATION)) {
                domainBuilder.withOperationTypeResolver(type, domainOperator, StaticDomainOperationTypeResolvers.widest(BigDecimal.class, Integer.class));
            }
        }

        domainBuilder.withOperationTypeResolver(String.class, DomainOperator.PLUS, StaticDomainOperationTypeResolvers.returning(String.class));
        domainBuilder.withOperationTypeResolver(Boolean.class, DomainOperator.NOT, StaticDomainOperationTypeResolvers.returning(Boolean.class));

        domainBuilder.withOperationTypeResolver(Calendar.class, DomainOperator.PLUS, StaticDomainOperationTypeResolvers.returning(Calendar.class));
        domainBuilder.withOperationTypeResolver(Calendar.class, DomainOperator.MINUS, StaticDomainOperationTypeResolvers.returning(Calendar.class));

        domainBuilder.createFunction("CURRENT_TIMESTAMP")
                .withExactArgumentCount(0)
                .withResultType(Calendar.class)
                .build();

        domainBuilder.createFunction("SUBSTRING")
                .withMinArgumentCount(2)
                .withResultType(String.class)
                .withArgument("string", String.class)
                .withArgument("start", Integer.class)
                .withArgument("end", Integer.class)
                .build();

        domainBuilder.createFunction("TRIM")
                .withMinArgumentCount(1)
                .withResultType(String.class)
                .withArgument("string", String.class)
                .withArgument("character", String.class)
                .build();

        domainBuilder.createFunction("LTRIM")
                .withMinArgumentCount(1)
                .withResultType(String.class)
                .withArgument("string", String.class)
                .withArgument("character", String.class)
                .build();

        domainBuilder.createFunction("RTRIM")
                .withMinArgumentCount(1)
                .withResultType(String.class)
                .withArgument("string", String.class)
                .withArgument("character", String.class)
                .build();

        domainBuilder.createFunction("UPPER")
                .withExactArgumentCount(1)
                .withResultType(String.class)
                .withArgument("string", String.class)
                .build();

        domainBuilder.createFunction("LOWER")
                .withExactArgumentCount(1)
                .withResultType(String.class)
                .withArgument("string", String.class)
                .build();

        domainBuilder.createFunction("LENGTH")
                .withExactArgumentCount(1)
                .withResultType(Integer.class)
                .withArgument("string", String.class)
                .build();

        domainBuilder.createFunction("LOCATE")
                .withMinArgumentCount(2)
                .withResultType(Integer.class)
                .withArgument("substring", String.class)
                .withArgument("string", String.class)
                .withArgument("start", Integer.class)
                .build();


        domainBuilder.createFunction("ABS")
                .withExactArgumentCount(1)
                .build();
        domainBuilder.withFunctionTypeResolver("ABS", StaticDomainFunctionTypeResolvers.FIRST_ARGUMENT_TYPE);

        for (String function : Arrays.asList("SQRT", "SIN", "COS", "TAN", "LOG", "EXP")) {
            domainBuilder.createFunction(function)
                    .withExactArgumentCount(1)
                    .build();
            domainBuilder.withFunctionTypeResolver(function, StaticDomainFunctionTypeResolvers.returning(BigDecimal.class));
        }

        domainBuilder.createFunction("POW")
                .withArgument("base", BigDecimal.class)
                .withArgument("power", BigDecimal.class)
                .build();
        domainBuilder.withFunctionTypeResolver("POW", StaticDomainFunctionTypeResolvers.returning(BigDecimal.class));

        domainBuilder.createFunction("GREATEST")
                .withMinArgumentCount(2)
                .build();
        domainBuilder.withFunctionTypeResolver("GREATEST", StaticDomainFunctionTypeResolvers.widest(BigDecimal.class));

        domainBuilder.createFunction("LEAST")
                .withMinArgumentCount(2)
                .build();
        domainBuilder.withFunctionTypeResolver("LEAST", StaticDomainFunctionTypeResolvers.widest(BigDecimal.class));

        domainBuilder.createFunction("SIZE")
                .withExactArgumentCount(1)
                .withCollectionArgument("collection")
                .withResultType(Integer.class)
                .build();
        domainBuilder.withFunctionTypeResolver("SIZE", new DomainFunctionTypeResolver() {
            @Override
            public DomainType resolveType(DomainModel domainModel, DomainFunction function, Map<DomainFunctionArgument, DomainType> argumentTypes) {
                DomainType argumentType = argumentTypes.values().iterator().next();
                if (!(argumentType instanceof CollectionDomainType)) {
                    throw new IllegalArgumentException("SIZE only accepts a collection argument! Invalid type given: " + argumentType);
                }
                return domainModel.getType(Integer.class);
            }
        });
    }

    private static void createBasicType(DomainBuilder domainBuilder, Class<?> type, DomainOperator[] operators, DomainPredicateType[] predicates) {
        String typeName = type.getSimpleName();
        domainBuilder.createBasicType(typeName, type);
        domainBuilder.withOperator(typeName, operators);
        domainBuilder.withPredicate(typeName, predicates);
    }
}
