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

package com.blazebit.notify.domain.impl.runtime.model;

import com.blazebit.notify.domain.impl.boot.model.DomainFunctionArgumentDefinitionImpl;
import com.blazebit.notify.domain.impl.boot.model.DomainFunctionDefinition;
import com.blazebit.notify.domain.impl.boot.model.MetamodelBuildingContext;
import com.blazebit.notify.domain.runtime.model.DomainFunction;
import com.blazebit.notify.domain.runtime.model.DomainFunctionArgument;
import com.blazebit.notify.domain.runtime.model.DomainType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DomainFunctionImpl implements DomainFunction {

    private final String name;
    private final int minArgumentCount;
    private final int argumentCount;
    private final DomainType resultType;
    private final List<DomainFunctionArgument> arguments;
    private final Map<Class<?>, Object> metadata;

    public DomainFunctionImpl(DomainFunctionDefinition functionDefinition, MetamodelBuildingContext context) {
        this.name = functionDefinition.getName();
        this.minArgumentCount = functionDefinition.getMinArgumentCount();
        this.argumentCount = functionDefinition.getArgumentCount();
        this.resultType = context.getType(functionDefinition.getResultTypeDefinition());
        List<DomainFunctionArgumentDefinitionImpl> argumentTypeDefinitions = functionDefinition.getArgumentDefinitions();
        List<DomainFunctionArgument> domainFunctionArguments = new ArrayList<>(argumentTypeDefinitions.size());
        for (int i = 0; i < argumentTypeDefinitions.size(); i++) {
            domainFunctionArguments.add(argumentTypeDefinitions.get(i).createFunctionArgument(this, context));
        }
        this.arguments = domainFunctionArguments;
        this.metadata = context.createMetadata(functionDefinition);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMinArgumentCount() {
        return minArgumentCount;
    }

    @Override
    public int getArgumentCount() {
        return argumentCount;
    }

    @Override
    public List<DomainFunctionArgument> getArguments() {
        return arguments;
    }

    @Override
    public DomainType getResultType() {
        return resultType;
    }

    @Override
    public <T> T getMetadata(Class<T> metadataType) {
        return (T) metadata.get(metadataType);
    }
}
