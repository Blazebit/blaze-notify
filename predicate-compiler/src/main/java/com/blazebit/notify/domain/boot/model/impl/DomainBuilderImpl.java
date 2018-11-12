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

package com.blazebit.notify.domain.boot.model.impl;

import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.boot.model.DomainFunctionBuilder;
import com.blazebit.notify.domain.runtime.model.DomainOperator;
import com.blazebit.notify.domain.runtime.model.DomainPredicateType;
import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.domain.runtime.model.impl.DomainModelImpl;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DomainBuilderImpl implements DomainBuilder {

    private Map<String, DomainType> domainTypes = new HashMap<>();
    private Map<String, Set<DomainOperator>> enabledOperators = new HashMap<>();
    private Map<String, Set<DomainPredicateType>> enabledPredicates = new HashMap<>();
    private Map<Class<?>, DomainType> domainTypesByJavaType = new HashMap<>();
    private Map<String, EntityDomainTypeDefinition> domainTypeDefinitions = new HashMap<>();
    private Map<Class<?>, EntityDomainTypeDefinition> domainTypeDefinitionsByJavaType = new HashMap<>();

    DomainBuilderImpl withDomainTypeDefinition(EntityDomainTypeDefinition domainTypeDefinition) {
        domainTypeDefinitions.put(domainTypeDefinition.getName(), domainTypeDefinition);
        if (domainTypeDefinition.getJavaType() != null) {
            domainTypeDefinitionsByJavaType.put(domainTypeDefinition.getJavaType(), domainTypeDefinition);
        }
        return this;
    }

    @Override
    public DomainBuilderImpl withDomainType(DomainType domainType) {
        domainTypes.put(domainType.getName(), domainType);
        if (domainType.getJavaType() != null) {
            domainTypesByJavaType.put(domainType.getJavaType(), domainType);
        }
        return this;
    }

    @Override
    public DomainBuilder withOperator(String typeName, DomainOperator operator) {
        Set<DomainOperator> domainOperators = enabledOperators.get(typeName);
        if (domainOperators == null) {
            domainOperators = EnumSet.of(operator);
            enabledOperators.put(typeName, domainOperators);
        } else {
            domainOperators.add(operator);
        }
        return this;
    }

    @Override
    public DomainBuilder withPredicate(String typeName, DomainPredicateType predicate) {
        Set<DomainPredicateType> domainPredicateTypes = enabledPredicates.get(typeName);
        if (domainPredicateTypes == null) {
            domainPredicateTypes = EnumSet.of(predicate);
            enabledPredicates.put(typeName, domainPredicateTypes);
        } else {
            domainPredicateTypes.add(predicate);
        }
        return this;
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
        // TODO: validate model and resolve it
        return new DomainModelImpl(domainTypes);
    }
}
