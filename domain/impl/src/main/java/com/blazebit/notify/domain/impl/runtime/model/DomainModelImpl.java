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

package com.blazebit.notify.domain.impl.runtime.model;

import com.blazebit.notify.domain.runtime.model.*;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DomainModelImpl implements DomainModel {

    private final Map<String, DomainType> domainTypes;
    private final Map<Class<?>, DomainType> domainTypesByJavaType;
    private final Map<DomainType, CollectionDomainType> collectionDomainTypes;
    private final Map<String, DomainFunction> domainFunctions;
    private final Map<String, DomainFunctionTypeResolver> domainFunctionTypeResolvers;
    private final Map<String, Map<DomainOperator, DomainOperationTypeResolver>> domainOperationTypeResolvers;
    private final Map<Class<?>, Map<DomainOperator, DomainOperationTypeResolver>> domainOperationTypeResolversByJavaType;
    private final Map<String, Map<DomainPredicateType, DomainPredicateTypeResolver>> domainPredicateTypeResolvers;
    private final Map<Class<?>, Map<DomainPredicateType, DomainPredicateTypeResolver>> domainPredicateTypeResolversByJavaType;
    private final NumericLiteralResolver numericLiteralResolver;
    private final BooleanLiteralResolver booleanLiteralResolver;
    private final StringLiteralResolver stringLiteralResolver;
    private final TemporalLiteralResolver temporalLiteralResolver;
    private final EnumLiteralResolver enumLiteralResolver;
    private final EntityLiteralResolver entityLiteralResolver;
    private final CollectionLiteralResolver collectionLiteralResolver;

    public DomainModelImpl(Map<String, DomainType> domainTypes, Map<Class<?>, DomainType> domainTypesByJavaType, Map<DomainType, CollectionDomainType> collectionDomainTypes, Map<String, DomainFunction> domainFunctions, Map<String, DomainFunctionTypeResolver> domainFunctionTypeResolvers, Map<String, Map<DomainOperator, DomainOperationTypeResolver>> domainOperationTypeResolvers, Map<Class<?>, Map<DomainOperator, DomainOperationTypeResolver>> domainOperationTypeResolversByJavaType, Map<String, Map<DomainPredicateType, DomainPredicateTypeResolver>> domainPredicateTypeResolvers, Map<Class<?>, Map<DomainPredicateType, DomainPredicateTypeResolver>> domainPredicateTypeResolversByJavaType, NumericLiteralResolver numericLiteralResolver, BooleanLiteralResolver booleanLiteralResolver, StringLiteralResolver stringLiteralResolver, TemporalLiteralResolver temporalLiteralResolver, EnumLiteralResolver enumLiteralResolver, EntityLiteralResolver entityLiteralResolver, CollectionLiteralResolver collectionLiteralResolver) {
        this.domainTypes = domainTypes;
        this.domainTypesByJavaType = domainTypesByJavaType;
        this.collectionDomainTypes = collectionDomainTypes;
        this.domainFunctions = domainFunctions;
        this.domainFunctionTypeResolvers = domainFunctionTypeResolvers;
        this.domainOperationTypeResolvers = domainOperationTypeResolvers;
        this.domainOperationTypeResolversByJavaType = domainOperationTypeResolversByJavaType;
        this.domainPredicateTypeResolvers = domainPredicateTypeResolvers;
        this.domainPredicateTypeResolversByJavaType = domainPredicateTypeResolversByJavaType;
        this.numericLiteralResolver = numericLiteralResolver;
        this.booleanLiteralResolver = booleanLiteralResolver;
        this.stringLiteralResolver = stringLiteralResolver;
        this.temporalLiteralResolver = temporalLiteralResolver;
        this.enumLiteralResolver = enumLiteralResolver;
        this.entityLiteralResolver = entityLiteralResolver;
        this.collectionLiteralResolver = collectionLiteralResolver;
    }

    @Override
    public DomainType getType(String name) {
        return domainTypes.get(name);
    }

    @Override
    public DomainType getType(Class<?> javaType) {
        return domainTypesByJavaType.get(javaType);
    }

    @Override
    public CollectionDomainType getCollectionType(DomainType elementDomainType) {
        return collectionDomainTypes.get(elementDomainType);
    }

    @Override
    public Map<String, DomainType> getTypes() {
        return domainTypes;
    }

    @Override
    public DomainFunction getFunction(String name) {
        return domainFunctions.get(name.toUpperCase());
    }

    @Override
    public DomainFunctionTypeResolver getFunctionTypeResolver(String functionName) {
        DomainFunctionTypeResolver typeResolver = domainFunctionTypeResolvers.get(functionName.toUpperCase());
        if (typeResolver == null) {
            return StaticDomainFunctionTypeResolver.INSTANCE;
        }
        return typeResolver;
    }

    @Override
    public DomainOperationTypeResolver getOperationTypeResolver(String typeName, DomainOperator operator) {
        Map<DomainOperator, DomainOperationTypeResolver> operationTypeResolverMap = domainOperationTypeResolvers.get(typeName);
        return operationTypeResolverMap == null ? null : operationTypeResolverMap.get(operator);
    }

    @Override
    public DomainOperationTypeResolver getOperationTypeResolver(Class<?> javaType, DomainOperator operator) {
        Map<DomainOperator, DomainOperationTypeResolver> operationTypeResolverMap = domainOperationTypeResolversByJavaType.get(javaType);
        return operationTypeResolverMap == null ? null : operationTypeResolverMap.get(operator);
    }

    @Override
    public DomainPredicateTypeResolver getPredicateTypeResolver(String typeName, DomainPredicateType predicateType) {
        Map<DomainPredicateType, DomainPredicateTypeResolver> predicateTypeResolverMap = domainPredicateTypeResolvers.get(typeName);
        return predicateTypeResolverMap == null ? null : predicateTypeResolverMap.get(predicateType);
    }

    @Override
    public DomainPredicateTypeResolver getPredicateTypeResolver(Class<?> javaType, DomainPredicateType predicateType) {
        Map<DomainPredicateType, DomainPredicateTypeResolver> predicateTypeResolverMap = domainPredicateTypeResolversByJavaType.get(javaType);
        return predicateTypeResolverMap == null ? null : predicateTypeResolverMap.get(predicateType);
    }

    @Override
    public NumericLiteralResolver getNumericLiteralResolver() {
        return numericLiteralResolver;
    }

    @Override
    public BooleanLiteralResolver getBooleanLiteralResolver() {
        return booleanLiteralResolver;
    }

    @Override
    public StringLiteralResolver getStringLiteralResolver() {
        return stringLiteralResolver;
    }

    @Override
    public TemporalLiteralResolver getTemporalLiteralResolver() {
        return temporalLiteralResolver;
    }

    @Override
    public EnumLiteralResolver getEnumLiteralResolver() {
        return enumLiteralResolver;
    }

    @Override
    public EntityLiteralResolver getEntityLiteralResolver() {
        return entityLiteralResolver;
    }

    @Override
    public CollectionLiteralResolver getCollectionLiteralResolver() {
        return collectionLiteralResolver;
    }
}
