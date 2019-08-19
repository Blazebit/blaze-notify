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

import com.blazebit.notify.domain.boot.model.CollectionDomainTypeDefinition;
import com.blazebit.notify.domain.boot.model.DomainTypeDefinition;
import com.blazebit.notify.domain.impl.runtime.model.CollectionDomainTypeImpl;
import com.blazebit.notify.domain.runtime.model.CollectionDomainType;
import com.blazebit.notify.domain.runtime.model.DomainType;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CollectionDomainTypeDefinitionImpl extends MetadataDefinitionHolderImpl<CollectionDomainTypeDefinition> implements CollectionDomainTypeDefinition, DomainTypeDefinitionImplementor<CollectionDomainTypeDefinition> {

    private final String name;
    private final Class<?> javaType;
    private final String elementTypeName;
    private final Class<?> elementTypeClass;
    private DomainTypeDefinition<?> elementTypeDefinition;
    private CollectionDomainType domainType;

    public CollectionDomainTypeDefinitionImpl(String name, Class<?> javaType, String elementTypeName, Class<?> elementTypeClass) {
        this.name = name;
        this.javaType = javaType;
        this.elementTypeName = elementTypeName;
        this.elementTypeClass = elementTypeClass;
    }

    public CollectionDomainTypeDefinitionImpl(String name, Class<?> javaType, DomainTypeDefinition<?> elementTypeDefinition) {
        this.name = name;
        this.javaType = javaType;
        this.elementTypeName = elementTypeDefinition.getName();
        this.elementTypeClass = elementTypeDefinition.getJavaType();
        this.elementTypeDefinition = elementTypeDefinition;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getJavaType() {
        return javaType;
    }

    @Override
    public DomainTypeDefinition<?> getElementType() {
        return elementTypeDefinition;
    }

    @Override
    public void bindTypes(DomainBuilderImpl domainBuilder, MetamodelBuildingContext context) {
        this.domainType = null;
        elementTypeDefinition = domainBuilder.getDomainTypeDefinition(elementTypeName);
        if (elementTypeDefinition == null) {
            elementTypeDefinition = domainBuilder.getDomainTypeDefinition(elementTypeClass);
            if (elementTypeDefinition == null) {
                context.addError("The element type '" + (elementTypeName == null ? elementTypeClass.getName() : elementTypeName) + "' defined for the collection type " + name + " is unknown!");
            }
        }
    }

    @Override
    public CollectionDomainType getType(MetamodelBuildingContext context) {
        if (domainType == null) {
            domainType = new CollectionDomainTypeImpl(this, context);
        }
        return domainType;
    }
}
