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

import com.blazebit.notify.domain.boot.model.DomainTypeDefinition;
import com.blazebit.notify.domain.runtime.model.EntityDomainTypeAttribute;
import com.blazebit.notify.domain.impl.runtime.model.EntityDomainTypeAttributeImpl;
import com.blazebit.notify.domain.impl.runtime.model.EntityDomainTypeImpl;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityDomainTypeAttributeDefinition extends MetadataDefinitionHolderImpl<EntityDomainTypeAttributeDefinition> {

    private final EntityDomainTypeDefinitionImpl owner;
    private final String name;
    private final String typeName;
    private final Class<?> javaType;
    private DomainTypeDefinition typeDefinition;
    private EntityDomainTypeAttribute attribute;

    public EntityDomainTypeAttributeDefinition(EntityDomainTypeDefinitionImpl owner, String name, String typeName, Class<?> javaType) {
        this.owner = owner;
        this.name = name;
        this.typeName = typeName;
        this.javaType = javaType;
    }

    public String getName() {
        return name;
    }

    public EntityDomainTypeDefinitionImpl getOwner() {
        return owner;
    }

    public String getTypeName() {
        return typeName;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public DomainTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }

    public void bindTypes(DomainBuilderImpl domainBuilder, MetamodelBuildingContext context) {
        this.attribute = null;
        typeDefinition = domainBuilder.getDomainTypeDefinition(typeName);
        if (typeDefinition == null) {
            context.addError("The type '" + typeName + "' defined for the attribute " + owner.getName() + "#" + name + " is unknown!");
        }
    }

    public EntityDomainTypeAttribute createAttribute(EntityDomainTypeImpl entityDomainType, MetamodelBuildingContext context) {
        if (attribute == null) {
            attribute = new EntityDomainTypeAttributeImpl(entityDomainType, this, context);
        }
        return attribute;
    }
}
