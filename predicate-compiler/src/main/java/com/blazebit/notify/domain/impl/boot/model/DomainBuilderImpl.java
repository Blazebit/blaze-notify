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
import com.blazebit.notify.domain.runtime.model.*;
import com.blazebit.notify.domain.impl.runtime.model.DomainModelImpl;

import java.util.*;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DomainBuilderImpl implements DomainBuilder {

    private Map<String, DomainType> domainTypes = new HashMap<>();
    private Map<String, DomainTypeDefinition> existingDomainTypeDefinitions = new HashMap<>();
    private Map<String, DomainFunctionDefinition> domainFunctionDefinitions = new HashMap<>();
    private Map<String, Set<DomainOperator>> enabledOperators = new HashMap<>();
    private Map<String, Set<DomainPredicateType>> enabledPredicates = new HashMap<>();
    private Map<Class<?>, DomainType> domainTypesByJavaType = new HashMap<>();
    private Map<String, EntityDomainTypeDefinitionImpl> domainTypeDefinitions = new HashMap<>();
    private Map<Class<?>, EntityDomainTypeDefinitionImpl> domainTypeDefinitionsByJavaType = new HashMap<>();

    DomainBuilderImpl withDomainTypeDefinition(EntityDomainTypeDefinitionImpl domainTypeDefinition) {
        domainTypeDefinitions.put(domainTypeDefinition.getName(), domainTypeDefinition);
        if (domainTypeDefinition.getJavaType() != null) {
            domainTypeDefinitionsByJavaType.put(domainTypeDefinition.getJavaType(), domainTypeDefinition);
        }
        return this;
    }

    DomainBuilderImpl withDomainFunctionDefinition(DomainFunctionDefinition domainFunctionDefinition) {
        domainFunctionDefinitions.put(domainFunctionDefinition.getName(), domainFunctionDefinition);
        return this;
    }

    public DomainTypeDefinition getDomainTypeDefinition(String typeName) {
        DomainTypeDefinition domainTypeDefinition = domainTypeDefinitions.get(typeName);
        if (domainTypeDefinition == null) {
            return existingDomainTypeDefinitions.get(typeName);
        }

        return domainTypeDefinition;
    }

    @Override
    public DomainBuilderImpl withDomainType(DomainType domainType) {
        domainTypes.put(domainType.getName(), domainType);
        existingDomainTypeDefinitions.put(domainType.getName(), new ExistingDomainTypeDefinition(domainType));
        if (domainType.getJavaType() != null) {
            domainTypesByJavaType.put(domainType.getJavaType(), domainType);
        }
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

    public Set<DomainOperator> getOperators(DomainTypeDefinition typeDefinition) {
        return getElements(enabledOperators, typeDefinition.getName());
    }

    public Set<DomainPredicateType> getPredicates(DomainTypeDefinition typeDefinition) {
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
    public EntityDomainTypeBuilderImpl createEntityType(String name) {
        return new EntityDomainTypeBuilderImpl(this, name, null);
    }

    @Override
    public EntityDomainTypeBuilderImpl createEntityType(String name, Class<?> javaType) {
        return new EntityDomainTypeBuilderImpl(this, name, javaType);
    }

    @Override
    public DomainModel build() {
        MetamodelBuildingContext context = new MetamodelBuildingContext(this);
        for (EntityDomainTypeDefinitionImpl typeDefinition : domainTypeDefinitions.values()) {
            typeDefinition.bindTypes(this, context);
        }
        for (DomainFunctionDefinition domainFunctionDefinition : domainFunctionDefinitions.values()) {
            domainFunctionDefinition.bindTypes(this, context);
        }

        Map<String, DomainType> domainTypes = new HashMap<>(this.domainTypes);
        if (!context.hasErrors()) {
            for (EntityDomainTypeDefinitionImpl typeDefinition : domainTypeDefinitions.values()) {
                domainTypes.put(typeDefinition.getName(), context.getType(typeDefinition));
            }
        }
        Map<String, DomainFunction> domainFunctions = new HashMap<>(domainFunctionDefinitions.size());
        if (!context.hasErrors()) {
            for (DomainFunctionDefinition functionDefinition : domainFunctionDefinitions.values()) {
                domainFunctions.put(functionDefinition.getName(), functionDefinition.getFunction(context));
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

        return new DomainModelImpl(domainTypes, domainFunctions);
    }
}
