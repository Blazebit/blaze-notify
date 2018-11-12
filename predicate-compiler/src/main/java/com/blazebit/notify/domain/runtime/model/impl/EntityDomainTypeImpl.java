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

package com.blazebit.notify.domain.runtime.model.impl;

import com.blazebit.notify.domain.runtime.model.DomainOperator;
import com.blazebit.notify.domain.runtime.model.DomainPredicateType;
import com.blazebit.notify.domain.runtime.model.EntityDomainType;
import com.blazebit.notify.domain.runtime.model.EntityDomainTypeAttribute;

import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityDomainTypeImpl extends AbstractDomainTypeImpl implements EntityDomainType {

    private final Map<String, EntityDomainTypeAttribute> attributes;

    public EntityDomainTypeImpl(Map<Class<?>, Object> metadata, String name, Class<?> javaType, Set<DomainOperator> enabledOperators, Set<DomainPredicateType> enabledPredicates, Map<String, EntityDomainTypeAttribute> attributes) {
        super(metadata, name, javaType, enabledOperators, enabledPredicates);
        this.attributes = attributes;
    }

    @Override
    public EntityDomainTypeAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Map<String, EntityDomainTypeAttribute> getAttributes() {
        return attributes;
    }
}
