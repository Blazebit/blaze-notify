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

import com.blazebit.notify.domain.boot.model.EntityDomainTypeBuilder;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityDomainTypeBuilderImpl implements EntityDomainTypeBuilder {

    private final DomainBuilderImpl domainBuilder;
    private final EntityDomainTypeDefinitionImpl domainTypeDefinition;

    public EntityDomainTypeBuilderImpl(DomainBuilderImpl domainBuilder, String name, Class<?> javaType) {
        this.domainBuilder = domainBuilder;
        this.domainTypeDefinition = new EntityDomainTypeDefinitionImpl(name, javaType);
    }

    @Override
    public EntityDomainTypeBuilderImpl addAttribute(String attributeName, String typeName) {
        domainTypeDefinition.addAttribute(new EntityDomainTypeAttributeDefinition(domainTypeDefinition, attributeName, typeName, null));
        return this;
    }

    @Override
    public EntityDomainTypeBuilderImpl addAttribute(String attributeName, Class<?> javaType) {
        domainTypeDefinition.addAttribute(new EntityDomainTypeAttributeDefinition(domainTypeDefinition, attributeName, null, javaType));
        return this;
    }

    @Override
    public DomainBuilderImpl build() {
        return domainBuilder.withDomainTypeDefinition(domainTypeDefinition);
    }
}
