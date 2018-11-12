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

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityDomainTypeAttributeDefinition {

    private final EntityDomainTypeDefinition owner;
    private final String name;
    private final String typeName;
    private final Class<?> javaType;

    public EntityDomainTypeAttributeDefinition(EntityDomainTypeDefinition owner, String name, String typeName, Class<?> javaType) {
        this.owner = owner;
        this.name = name;
        this.typeName = typeName;
        this.javaType = javaType;
    }

    public String getName() {
        return name;
    }

    public EntityDomainTypeDefinition getOwner() {
        return owner;
    }

    public String getTypeName() {
        return typeName;
    }

    public Class<?> getJavaType() {
        return javaType;
    }
}
