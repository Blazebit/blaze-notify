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
import com.blazebit.notify.domain.boot.model.DomainTypeDefinition;
import com.blazebit.notify.domain.boot.model.impl.MetamodelBuildingContext;
import com.blazebit.notify.domain.runtime.model.BasicDomainType;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class BasicDomainTypeImpl extends AbstractDomainTypeImpl implements BasicDomainType {

    private final Map<Class<?>, Object> metadata;

    public BasicDomainTypeImpl(DomainTypeDefinition typeDefinition, MetamodelBuildingContext context) {
        super(typeDefinition, context);
        this.metadata = context.createMetadata(typeDefinition);
    }

    @Override
    public <T> T getMetadata(Class<T> metadataType) {
        return (T) metadata.get(metadataType);
    }
}
