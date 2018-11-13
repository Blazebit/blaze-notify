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
import com.blazebit.notify.domain.boot.model.MetadataDefinition;
import com.blazebit.notify.domain.boot.model.MetadataDefinitionHolder;
import com.blazebit.notify.domain.runtime.model.DomainType;

import java.util.Collections;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ExistingDomainTypeDefinitionImpl implements DomainTypeDefinition<ExistingDomainTypeDefinitionImpl> {

    private final DomainType domainType;

    public ExistingDomainTypeDefinitionImpl(DomainType domainType) {
        this.domainType = domainType;
    }

    @Override
    public ExistingDomainTypeDefinitionImpl withMetadataDefinition(MetadataDefinition metadataDefinition) {
        // TODO: throw exception?
        return this;
    }

    @Override
    public String getName() {
        return domainType.getName();
    }

    @Override
    public Class<?> getJavaType() {
        return domainType.getJavaType();
    }

    @Override
    public DomainType getType(MetamodelBuildingContext context) {
        return domainType;
    }

    @Override
    public Map<Class<?>, MetadataDefinition<?>> getMetadataDefinitions() {
        return Collections.emptyMap();
    }
}
