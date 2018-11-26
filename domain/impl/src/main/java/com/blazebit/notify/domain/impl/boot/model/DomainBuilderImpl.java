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

package com.blazebit.notify.domain.impl.boot.model;

import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.boot.model.DomainFunctionBuilder;
import com.blazebit.notify.domain.boot.model.DomainTypeDefinition;
import com.blazebit.notify.domain.boot.model.MetadataDefinition;
import com.blazebit.notify.domain.impl.runtime.model.DomainModelImpl;
import com.blazebit.notify.domain.runtime.model.*;

import java.util.*;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DomainBuilderImpl implements DomainBuilder {

    private Map<String, DomainFunctionDefinitionImpl> domainFunctionDefinitions = new HashMap<>();
    private Map<String, Set<DomainOperator>> enabledOperators = new HashMap<>();
    private Map<String, Set<DomainPredicateType>> enabledPredicates = new HashMap<>();
    private Map<String, DomainTypeDefinitionImplementor<?>> domainTypeDefinitions = new HashMap<>();
    private Map<Class<?>, DomainTypeDefinitionImplementor<?>> domainTypeDefinitionsByJavaType = new HashMap<>();
    private Map<String, DomainFunctionTypeResolver> domainFunctionTypeResolvers = new HashMap<>();
    private Map<String, Map<DomainOperator, DomainOperationTypeResolver>> domainOperationTypeResolvers = new HashMap<>();
    private Map<Class<?>, Map<DomainOperator, DomainOperationTypeResolver>> domainOperationTypeResolversByJavaType = new HashMap<>();
    private Map<String, Map<DomainPredicateType, DomainPredicateTypeResolver>> domainPredicateTypeResolvers = new HashMap<>();
    private Map<Class<?>, Map<DomainPredicateType, DomainPredicateTypeResolver>> domainPredicateTypeResolversByJavaType = new HashMap<>();
    private NumericLiteralTypeResolver numericLiteralTypeResolver;
    private BooleanLiteralTypeResolver booleanLiteralTypeResolver;
    private StringLiteralTypeResolver stringLiteralTypeResolver;
    private TemporalLiteralTypeResolver temporalLiteralTypeResolver;
    private EnumLiteralTypeResolver enumLiteralTypeResolver;

    DomainBuilderImpl withDomainTypeDefinition(DomainTypeDefinitionImplementor<?> domainTypeDefinition) {
        domainTypeDefinitions.put(domainTypeDefinition.getName(), domainTypeDefinition);
        if (domainTypeDefinition.getJavaType() != null) {
            domainTypeDefinitionsByJavaType.put(domainTypeDefinition.getJavaType(), domainTypeDefinition);
        }
        return this;
    }

    DomainBuilderImpl withDomainFunctionDefinition(DomainFunctionDefinitionImpl domainFunctionDefinition) {
        domainFunctionDefinitions.put(domainFunctionDefinition.getName(), domainFunctionDefinition);
        return this;
    }

    public DomainTypeDefinition<?> getDomainTypeDefinition(String typeName) {
        return domainTypeDefinitions.get(typeName);
    }

    public DomainTypeDefinition<?> getDomainTypeDefinition(Class<?> javaType) {
        return domainTypeDefinitionsByJavaType.get(javaType);
    }

    public DomainTypeDefinition<?> getCollectionDomainTypeDefinition(DomainTypeDefinition<?> typeDefinition) {
        if (typeDefinition == null) {
            return null;
        }
        return new CollectionDomainTypeDefinitionImpl("Collection", Collection.class, typeDefinition);
    }

    @Override
    public DomainBuilder withLiteralTypeResolver(BooleanLiteralTypeResolver typeResolver) {
        this.booleanLiteralTypeResolver = typeResolver;
        return this;
    }

    @Override
    public DomainBuilder withLiteralTypeResolver(NumericLiteralTypeResolver typeResolver) {
        this.numericLiteralTypeResolver = typeResolver;
        return this;
    }

    @Override
    public DomainBuilder withLiteralTypeResolver(StringLiteralTypeResolver typeResolver) {
        this.stringLiteralTypeResolver = typeResolver;
        return this;
    }

    @Override
    public DomainBuilder withLiteralTypeResolver(TemporalLiteralTypeResolver typeResolver) {
        this.temporalLiteralTypeResolver = typeResolver;
        return this;
    }

    @Override
    public DomainBuilder withLiteralTypeResolver(EnumLiteralTypeResolver typeResolver) {
        this.enumLiteralTypeResolver = typeResolver;
        return this;
    }

    @Override
    public DomainBuilder withFunctionTypeResolver(String functionName, DomainFunctionTypeResolver functionTypeResolver) {
        domainFunctionTypeResolvers.put(functionName, functionTypeResolver);
        return this;
    }

    @Override
    public DomainBuilder withOperationTypeResolver(String typeName, DomainOperator operator, DomainOperationTypeResolver operationTypeResolver) {
        Map<DomainOperator, DomainOperationTypeResolver> operationTypeResolverMap = domainOperationTypeResolvers.get(typeName);
        if (operationTypeResolverMap == null) {
            operationTypeResolverMap = new HashMap<>();
            domainOperationTypeResolvers.put(typeName, operationTypeResolverMap);
        }
        operationTypeResolverMap.put(operator, operationTypeResolver);
        return this;
    }

    @Override
    public DomainBuilder withOperationTypeResolver(Class<?> javaType, DomainOperator operator, DomainOperationTypeResolver operationTypeResolver) {
        Map<DomainOperator, DomainOperationTypeResolver> operationTypeResolverMap = domainOperationTypeResolversByJavaType.get(javaType);
        if (operationTypeResolverMap == null) {
            operationTypeResolverMap = new HashMap<>();
            domainOperationTypeResolversByJavaType.put(javaType, operationTypeResolverMap);
        }
        operationTypeResolverMap.put(operator, operationTypeResolver);
        return this;
    }

    @Override
    public DomainBuilder withPredicateTypeResolver(String typeName, DomainPredicateType operator, DomainPredicateTypeResolver predicateTypeResolver) {
        Map<DomainPredicateType, DomainPredicateTypeResolver> operationTypeResolverMap = domainPredicateTypeResolvers.get(typeName);
        if (operationTypeResolverMap == null) {
            operationTypeResolverMap = new HashMap<>();
            domainPredicateTypeResolvers.put(typeName, operationTypeResolverMap);
        }
        operationTypeResolverMap.put(operator, predicateTypeResolver);
        return this;
    }

    @Override
    public DomainBuilder withPredicateTypeResolver(Class<?> javaType, DomainPredicateType operator, DomainPredicateTypeResolver predicateTypeResolver) {
        Map<DomainPredicateType, DomainPredicateTypeResolver> operationTypeResolverMap = domainPredicateTypeResolversByJavaType.get(javaType);
        if (operationTypeResolverMap == null) {
            operationTypeResolverMap = new HashMap<>();
            domainPredicateTypeResolversByJavaType.put(javaType, operationTypeResolverMap);
        }
        operationTypeResolverMap.put(operator, predicateTypeResolver);
        return this;
    }

    @Override
    public DomainBuilder withOperator(String typeName, DomainOperator operator) {
        return withElement(enabledOperators, typeName, operator);
    }

    @Override
    public DomainBuilder withPredicate(String typeName, DomainPredicateType predicate) {
        return withElement(enabledPredicates, typeName, predicate);
    }

    @Override
    public DomainBuilder withOperator(String typeName, DomainOperator... operators) {
        return withElements(enabledOperators, typeName, operators);
    }

    @Override
    public DomainBuilder withPredicate(String typeName, DomainPredicateType... predicates) {
        return withElements(enabledPredicates, typeName, predicates);
    }

    public Set<DomainOperator> getOperators(DomainTypeDefinition<?> typeDefinition) {
        return getElements(enabledOperators, typeDefinition.getName());
    }

    public Set<DomainPredicateType> getPredicates(DomainTypeDefinition<?> typeDefinition) {
        return getElements(enabledPredicates, typeDefinition.getName());
    }

    private <T extends Enum<T>> DomainBuilder withElement(Map<String, Set<T>> map, String typeName, T predicate) {
        Set<T> domainPredicates = map.get(typeName);
        if (domainPredicates == null) {
            domainPredicates = EnumSet.of(predicate);
            map.put(typeName, domainPredicates);
        } else {
            domainPredicates.add(predicate);
        }
        return this;
    }

    private <T extends Enum<T>> DomainBuilder withElements(Map<String, Set<T>> map, String typeName, T... predicates) {
        Set<T> domainPredicates = map.get(typeName);
        if (domainPredicates == null) {
            domainPredicates = EnumSet.noneOf((Class<T>) predicates[0].getClass());
            map.put(typeName, domainPredicates);
        }

        for (int i = 0; i < predicates.length; i++) {
            T predicate = predicates[i];
            domainPredicates.add(predicate);
        }

        return this;
    }

    private <T> Set<T> getElements(Map<String, Set<T>> map, String typeName) {
        Set<T> set = map.get(typeName);
        if (set == null) {
            return Collections.emptySet();
        }
        return set;
    }

    @Override
    public DomainFunctionBuilder createFunction(String name) {
        return new DomainFunctionBuilderImpl(this, name);
    }

    @Override
    public DomainBuilder createBasicType(String name) {
        return withDomainTypeDefinition(new BasicDomainTypeDefinitionImpl(name, null));
    }

    @Override
    public DomainBuilder createBasicType(String name, Class<?> javaType) {
        return withDomainTypeDefinition(new BasicDomainTypeDefinitionImpl(name, javaType));
    }

    @Override
    public DomainBuilder createBasicType(String name, MetadataDefinition<?>... metadataDefinitions) {
        BasicDomainTypeDefinitionImpl basicDomainTypeDefinition = new BasicDomainTypeDefinitionImpl(name, null);
        for (MetadataDefinition<?> metadataDefinition : metadataDefinitions) {
            basicDomainTypeDefinition.withMetadataDefinition(metadataDefinition);
        }

        return withDomainTypeDefinition(basicDomainTypeDefinition);
    }

    @Override
    public DomainBuilder createBasicType(String name, Class<?> javaType, MetadataDefinition<?>... metadataDefinitions) {
        BasicDomainTypeDefinitionImpl basicDomainTypeDefinition = new BasicDomainTypeDefinitionImpl(name, javaType);
        for (MetadataDefinition<?> metadataDefinition : metadataDefinitions) {
            basicDomainTypeDefinition.withMetadataDefinition(metadataDefinition);
        }

        return withDomainTypeDefinition(basicDomainTypeDefinition);
    }

    @Override
    public EntityDomainTypeBuilderImpl createEntityType(String name) {
        return new EntityDomainTypeBuilderImpl(this, name, null);
    }

    @Override
    public EntityDomainTypeBuilderImpl createEntityType(String name, Class<?> javaType) {
        return new EntityDomainTypeBuilderImpl(this, name, javaType);
    }

    @Override
    public EnumDomainTypeBuilderImpl createEnumType(String name) {
        return new EnumDomainTypeBuilderImpl(this, name, null);
    }

    @Override
    public EnumDomainTypeBuilderImpl createEnumType(String name, Class<? extends Enum<?>> javaType) {
        return new EnumDomainTypeBuilderImpl(this, name, javaType);
    }

    @Override
    public DomainModel build() {
        MetamodelBuildingContext context = new MetamodelBuildingContext(this);
        for (DomainTypeDefinitionImplementor<?> typeDefinition : domainTypeDefinitions.values()) {
            typeDefinition.bindTypes(this, context);
        }
        for (DomainFunctionDefinitionImpl domainFunctionDefinition : domainFunctionDefinitions.values()) {
            domainFunctionDefinition.bindTypes(this, context);
        }

        Map<String, DomainType> domainTypes = new HashMap<>(domainTypeDefinitions.size());
        Map<Class<?>, DomainType> domainTypesByJavaType = new HashMap<>(domainTypeDefinitions.size());
        if (!context.hasErrors()) {
            for (DomainTypeDefinitionImplementor<?> typeDefinition : domainTypeDefinitions.values()) {
                DomainType domainType = context.getType(typeDefinition);
                domainTypes.put(typeDefinition.getName(), domainType);
                if (typeDefinition.getJavaType() != null) {
                    domainTypesByJavaType.put(domainType.getJavaType(), domainType);
                }
            }
        }
        Map<String, DomainFunction> domainFunctions = new HashMap<>(domainFunctionDefinitions.size());
        if (!context.hasErrors()) {
            for (DomainFunctionDefinitionImpl functionDefinition : domainFunctionDefinitions.values()) {
                domainFunctions.put(functionDefinition.getName().toUpperCase(), functionDefinition.getFunction(context));
            }
        }
        Map<String, DomainFunctionTypeResolver> domainFunctionTypeResolvers = new HashMap<>(this.domainFunctionTypeResolvers.size());
        if (!context.hasErrors()) {
            for (Map.Entry<String, DomainFunctionTypeResolver> entry : this.domainFunctionTypeResolvers.entrySet()) {
                String name = entry.getKey().toUpperCase();
                domainFunctionTypeResolvers.put(name, entry.getValue());
                if (!domainFunctions.containsKey(name)) {
                    context.addError("A function type resolver was registered but no function with the name '" + entry.getKey() + "' was found: " + entry.getValue());
                }
            }
        }

        Map<String, Map<DomainOperator, DomainOperationTypeResolver>> domainOperationTypeResolvers = new HashMap<>(this.domainOperationTypeResolvers.size());
        Map<Class<?>, Map<DomainOperator, DomainOperationTypeResolver>> domainOperationTypeResolversByJavaType = new HashMap<>(this.domainOperationTypeResolversByJavaType.size());
        if (!context.hasErrors()) {
            for (Map.Entry<String, Map<DomainOperator, DomainOperationTypeResolver>> entry : this.domainOperationTypeResolvers.entrySet()) {
                String typeName = entry.getKey();
                DomainType domainType = domainTypes.get(typeName);
                if (domainType == null) {
                    context.addError("An operation type resolver was registered but no type with the name '" + typeName + "' was found: " + entry.getValue());
                } else {
                    Map<DomainOperator, DomainOperationTypeResolver> operationTypeResolverMap = new HashMap<>(entry.getValue().size());
                    domainOperationTypeResolvers.put(typeName, operationTypeResolverMap);

                    Map<DomainOperator, DomainOperationTypeResolver> operationTypeResolverMapByJavaType = domainOperationTypeResolversByJavaType.get(domainType.getJavaType());
                    if (operationTypeResolverMapByJavaType == null && domainType.getJavaType() != null) {
                        operationTypeResolverMapByJavaType = new HashMap<>();
                        domainOperationTypeResolversByJavaType.put(domainType.getJavaType(), operationTypeResolverMapByJavaType);
                    }

                    for (Map.Entry<DomainOperator, DomainOperationTypeResolver> resolverEntry : entry.getValue().entrySet()) {
                        if (domainType.getEnabledOperators().contains(resolverEntry.getKey())) {
                            operationTypeResolverMap.put(resolverEntry.getKey(), resolverEntry.getValue());
                            if (operationTypeResolverMapByJavaType != null) {
                                operationTypeResolverMapByJavaType.put(resolverEntry.getKey(), resolverEntry.getValue());
                            }
                        } else {
                            context.addError("An operation type resolver for the type with the name '" + typeName + "' was registered for a non enabled operator '" + resolverEntry.getKey() + "': " + resolverEntry.getValue());
                        }
                    }
                }
            }
        }

        if (!context.hasErrors()) {
            for (Map.Entry<Class<?>, Map<DomainOperator, DomainOperationTypeResolver>> entry : this.domainOperationTypeResolversByJavaType.entrySet()) {
                Class<?> javaType = entry.getKey();
                DomainType domainType = domainTypesByJavaType.get(javaType);
                if (domainType == null) {
                    context.addError("An operation type resolver was registered but no type with the java type '" + javaType + "' was found: " + entry.getValue());
                } else {
                    Map<DomainOperator, DomainOperationTypeResolver> operationTypeResolverMap = domainOperationTypeResolvers.get(domainType.getName());
                    if (operationTypeResolverMap == null) {
                        operationTypeResolverMap = new HashMap<>(entry.getValue().size());
                        domainOperationTypeResolvers.put(domainType.getName(), operationTypeResolverMap);
                    }

                    Map<DomainOperator, DomainOperationTypeResolver> operationTypeResolverMapByJavaType = domainOperationTypeResolversByJavaType.get(domainType.getJavaType());
                    if (operationTypeResolverMapByJavaType == null) {
                        operationTypeResolverMapByJavaType = new HashMap<>();
                        domainOperationTypeResolversByJavaType.put(domainType.getJavaType(), operationTypeResolverMapByJavaType);
                    }

                    for (Map.Entry<DomainOperator, DomainOperationTypeResolver> resolverEntry : entry.getValue().entrySet()) {
                        if (domainType.getEnabledOperators().contains(resolverEntry.getKey())) {
                            operationTypeResolverMap.put(resolverEntry.getKey(), resolverEntry.getValue());
                            operationTypeResolverMapByJavaType.put(resolverEntry.getKey(), resolverEntry.getValue());
                        } else {
                            context.addError("An operation type resolver for the type with the java type '" + javaType + "' was registered for a non enabled operator '" + resolverEntry.getKey() + "': " + resolverEntry.getValue());
                        }
                    }
                }
            }
        }

        Map<String, Map<DomainPredicateType, DomainPredicateTypeResolver>> domainPredicateTypeResolvers = new HashMap<>(this.domainPredicateTypeResolvers.size());
        Map<Class<?>, Map<DomainPredicateType, DomainPredicateTypeResolver>> domainPredicateTypeResolversByJavaType = new HashMap<>(this.domainPredicateTypeResolversByJavaType.size());
        if (!context.hasErrors()) {
            for (Map.Entry<String, Map<DomainPredicateType, DomainPredicateTypeResolver>> entry : this.domainPredicateTypeResolvers.entrySet()) {
                String typeName = entry.getKey();
                DomainType domainType = domainTypes.get(typeName);
                if (domainType == null) {
                    context.addError("An operation type resolver was registered but no type with the name '" + typeName + "' was found: " + entry.getValue());
                } else {
                    Map<DomainPredicateType, DomainPredicateTypeResolver> predicateTypeResolverMap = new HashMap<>(entry.getValue().size());
                    domainPredicateTypeResolvers.put(typeName, predicateTypeResolverMap);

                    Map<DomainPredicateType, DomainPredicateTypeResolver> predicateTypeResolverMapByJavaType = domainPredicateTypeResolversByJavaType.get(domainType.getJavaType());
                    if (predicateTypeResolverMapByJavaType == null && domainType.getJavaType() != null) {
                        predicateTypeResolverMapByJavaType = new HashMap<>();
                        domainPredicateTypeResolversByJavaType.put(domainType.getJavaType(), predicateTypeResolverMapByJavaType);
                    }

                    for (Map.Entry<DomainPredicateType, DomainPredicateTypeResolver> resolverEntry : entry.getValue().entrySet()) {
                        if (domainType.getEnabledPredicates().contains(resolverEntry.getKey())) {
                            predicateTypeResolverMap.put(resolverEntry.getKey(), resolverEntry.getValue());
                            if (predicateTypeResolverMapByJavaType != null) {
                                predicateTypeResolverMapByJavaType.put(resolverEntry.getKey(), resolverEntry.getValue());
                            }
                        } else {
                            context.addError("A predicate type resolver for the type with the name '" + typeName + "' was registered for a non enabled predicate '" + resolverEntry.getKey() + "': " + resolverEntry.getValue());
                        }
                    }
                }
            }
        }

        if (!context.hasErrors()) {
            for (Map.Entry<Class<?>, Map<DomainPredicateType, DomainPredicateTypeResolver>> entry : this.domainPredicateTypeResolversByJavaType.entrySet()) {
                Class<?> javaType = entry.getKey();
                DomainType domainType = domainTypesByJavaType.get(javaType);
                if (domainType == null) {
                    context.addError("An operation type resolver was registered but no type with the java type '" + javaType + "' was found: " + entry.getValue());
                } else {
                    Map<DomainPredicateType, DomainPredicateTypeResolver> predicateTypeResolverMap = domainPredicateTypeResolvers.get(domainType.getName());
                    if (predicateTypeResolverMap == null) {
                        predicateTypeResolverMap = new HashMap<>(entry.getValue().size());
                        domainPredicateTypeResolvers.put(domainType.getName(), predicateTypeResolverMap);
                    }

                    Map<DomainPredicateType, DomainPredicateTypeResolver> predicateTypeResolverMapByJavaType = domainPredicateTypeResolversByJavaType.get(domainType.getJavaType());
                    if (predicateTypeResolverMapByJavaType == null) {
                        predicateTypeResolverMapByJavaType = new HashMap<>();
                        domainPredicateTypeResolversByJavaType.put(domainType.getJavaType(), predicateTypeResolverMapByJavaType);
                    }

                    for (Map.Entry<DomainPredicateType, DomainPredicateTypeResolver> resolverEntry : entry.getValue().entrySet()) {
                        if (domainType.getEnabledPredicates().contains(resolverEntry.getKey())) {
                            predicateTypeResolverMap.put(resolverEntry.getKey(), resolverEntry.getValue());
                            predicateTypeResolverMapByJavaType.put(resolverEntry.getKey(), resolverEntry.getValue());
                        } else {
                            context.addError("An operation type resolver for the type with the java type '" + javaType + "' was registered for a non enabled operator '" + resolverEntry.getKey() + "': " + resolverEntry.getValue());
                        }
                    }
                }
            }
        }

        if (!context.hasErrors()) {
            for (DomainType domainType : domainTypes.values()) {
                Map<DomainOperator, DomainOperationTypeResolver> operationTypeResolverMap = domainOperationTypeResolvers.get(domainType.getName());
                if (operationTypeResolverMap == null && !domainType.getEnabledOperators().isEmpty()) {
                    operationTypeResolverMap = new HashMap<>();
                    domainOperationTypeResolvers.put(domainType.getName(), operationTypeResolverMap);
                }
                for (DomainOperator enabledOperator : domainType.getEnabledOperators()) {
                    if (!operationTypeResolverMap.containsKey(enabledOperator)) {
                        // TODO: Maybe throw an error instead?
                        operationTypeResolverMap.put(enabledOperator, StaticDomainOperationTypeResolvers.returning(domainType.getName()));
                    }
                }

                Map<DomainPredicateType, DomainPredicateTypeResolver> predicateTypeResolverMap = domainPredicateTypeResolvers.get(domainType.getName());
                if (predicateTypeResolverMap == null && !domainType.getEnabledPredicates().isEmpty()) {
                    predicateTypeResolverMap = new HashMap<>();
                    domainPredicateTypeResolvers.put(domainType.getName(), predicateTypeResolverMap);
                }
                for (DomainPredicateType enabledPredicate : domainType.getEnabledPredicates()) {
                    if (!predicateTypeResolverMap.containsKey(enabledPredicate)) {
                        predicateTypeResolverMap.put(enabledPredicate, StaticDomainPredicateTypeResolvers.returning(Boolean.class));
                    }
                }
            }
        }

        if (context.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Couldn't build the domain model because of some errors:");
            for (String error : context.getErrors()) {
                sb.append('\n').append(error);
            }

            throw new IllegalArgumentException(sb.toString());
        }

        return new DomainModelImpl(domainTypes, domainTypesByJavaType, domainFunctions, domainFunctionTypeResolvers, domainOperationTypeResolvers, domainOperationTypeResolversByJavaType, domainPredicateTypeResolvers, domainPredicateTypeResolversByJavaType, numericLiteralTypeResolver, booleanLiteralTypeResolver, stringLiteralTypeResolver, temporalLiteralTypeResolver, enumLiteralTypeResolver);
    }
}
