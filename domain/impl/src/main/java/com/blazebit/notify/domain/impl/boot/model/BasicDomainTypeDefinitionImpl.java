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

import com.blazebit.notify.domain.boot.model.BasicDomainTypeDefinition;
import com.blazebit.notify.domain.boot.model.EntityDomainTypeAttributeDefinition;
import com.blazebit.notify.domain.boot.model.EntityDomainTypeDefinition;
import com.blazebit.notify.domain.impl.runtime.model.BasicDomainTypeImpl;
import com.blazebit.notify.domain.impl.runtime.model.EntityDomainTypeImpl;
import com.blazebit.notify.domain.runtime.model.BasicDomainType;
import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.domain.runtime.model.EntityDomainType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class BasicDomainTypeDefinitionImpl extends MetadataDefinitionHolderImpl<BasicDomainTypeDefinition> implements BasicDomainTypeDefinition, DomainTypeDefinitionImplementor<BasicDomainTypeDefinition> {

    private final String name;
    private final Class<?> javaType;
    private BasicDomainType domainType;

    public BasicDomainTypeDefinitionImpl(String name, Class<?> javaType) {
        this.name = name;
        this.javaType = javaType;
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
    public void bindTypes(DomainBuilderImpl domainBuilder, MetamodelBuildingContext context) {
    }

    @Override
    public DomainType getType(MetamodelBuildingContext context) {
        if (domainType == null) {
            domainType = new BasicDomainTypeImpl(this, context);
        }
        return domainType;
    }
}
