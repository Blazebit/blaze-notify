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

import com.blazebit.notify.domain.boot.model.EntityDomainTypeDefinition;
import com.blazebit.notify.domain.boot.model.impl.EntityDomainTypeAttributeDefinition;
import com.blazebit.notify.domain.boot.model.impl.MetamodelBuildingContext;
import com.blazebit.notify.domain.runtime.model.EntityDomainType;
import com.blazebit.notify.domain.runtime.model.EntityDomainTypeAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityDomainTypeImpl extends AbstractDomainTypeImpl implements EntityDomainType {

    private final Map<String, EntityDomainTypeAttribute> attributes;
    private final Map<Class<?>, Object> metadata;

    public EntityDomainTypeImpl(EntityDomainTypeDefinition typeDefinition, MetamodelBuildingContext context) {
        super(typeDefinition, context);
        Map<String, EntityDomainTypeAttribute> attributes = new HashMap<>(typeDefinition.getAttributes().size());
        for (EntityDomainTypeAttributeDefinition attributeDefinition : typeDefinition.getAttributes().values()) {
            attributes.put(attributeDefinition.getName(), attributeDefinition.createAttribute(this, context));
        }
        this.attributes = attributes;
        this.metadata = context.createMetadata(typeDefinition);
    }

    @Override
    public EntityDomainTypeAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Map<String, EntityDomainTypeAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public <T> T getMetadata(Class<T> metadataType) {
        return (T) metadata.get(metadataType);
    }
}