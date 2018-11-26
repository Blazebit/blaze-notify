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

package com.blazebit.notify.domain.boot.model;

import com.blazebit.notify.domain.runtime.model.*;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface DomainBuilder {

    public DomainBuilder withLiteralTypeResolver(BooleanLiteralTypeResolver typeResolver);

    public DomainBuilder withLiteralTypeResolver(NumericLiteralTypeResolver typeResolver);

    public DomainBuilder withLiteralTypeResolver(StringLiteralTypeResolver typeResolver);

    public DomainBuilder withLiteralTypeResolver(TemporalLiteralTypeResolver typeResolver);

    public DomainBuilder withLiteralTypeResolver(EnumLiteralTypeResolver typeResolver);

    public DomainBuilder withFunctionTypeResolver(String functionName, DomainFunctionTypeResolver functionTypeResolver);

    public DomainBuilder withOperationTypeResolver(String typeName, DomainOperator operator, DomainOperationTypeResolver operationTypeResolver);

    public DomainBuilder withOperationTypeResolver(Class<?> javaType, DomainOperator operator, DomainOperationTypeResolver operationTypeResolver);

    public DomainBuilder withPredicateTypeResolver(String typeName, DomainPredicateType operator, DomainPredicateTypeResolver predicateTypeResolver);

    public DomainBuilder withPredicateTypeResolver(Class<?> javaType, DomainPredicateType operator, DomainPredicateTypeResolver predicateTypeResolver);

    public DomainBuilder withOperator(String typeName, DomainOperator operator);

    public DomainBuilder withOperator(String typeName, DomainOperator... operators);

    public DomainBuilder withPredicate(String typeName, DomainPredicateType predicate);

    public DomainBuilder withPredicate(String typeName, DomainPredicateType... predicates);

    public DomainFunctionBuilder createFunction(String name);

    public DomainBuilder createBasicType(String name);

    public DomainBuilder createBasicType(String name, Class<?> javaType);

    public DomainBuilder createBasicType(String name, MetadataDefinition<?>... metadataDefinitions);

    public DomainBuilder createBasicType(String name, Class<?> javaType, MetadataDefinition<?>... metadataDefinitions);

    public EntityDomainTypeBuilder createEntityType(String name);

    public EntityDomainTypeBuilder createEntityType(String name, Class<?> javaType);

    public EnumDomainTypeBuilder createEnumType(String name);

    public EnumDomainTypeBuilder createEnumType(String name, Class<? extends Enum<?>> javaType);

    public DomainModel build();
}
