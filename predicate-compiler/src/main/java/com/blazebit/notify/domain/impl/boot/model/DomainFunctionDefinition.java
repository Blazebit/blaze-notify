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
import com.blazebit.notify.domain.runtime.model.DomainFunction;
import com.blazebit.notify.domain.impl.runtime.model.DomainFunctionImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DomainFunctionDefinition extends MetadataDefinitionHolderImpl<DomainFunctionDefinition> {

    private final String name;
    private int minArgumentCount;
    private int argumentCount;
    private List<String> argumentNames = new ArrayList<>();
    private List<String> argumentTypeNames = new ArrayList<>();
    private String resultTypeName;
    private List<DomainFunctionArgumentDefinitionImpl> argumentDefinitions;
    private DomainTypeDefinition resultTypeDefinition;
    private DomainFunction function;

    public DomainFunctionDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getMinArgumentCount() {
        return minArgumentCount;
    }

    public void setMinArgumentCount(int minArgumentCount) {
        this.minArgumentCount = minArgumentCount;
    }

    public int getArgumentCount() {
        return argumentCount;
    }

    public void setArgumentCount(int argumentCount) {
        this.argumentCount = argumentCount;
    }

    public List<String> getArgumentNames() {
        return argumentNames;
    }

    public void setArgumentNames(List<String> argumentNames) {
        this.argumentNames = argumentNames;
    }

    public List<String> getArgumentTypeNames() {
        return argumentTypeNames;
    }

    public void setArgumentTypeNames(List<String> argumentTypeNames) {
        this.argumentTypeNames = argumentTypeNames;
    }

    public String getResultTypeName() {
        return resultTypeName;
    }

    public void setResultTypeName(String resultTypeName) {
        this.resultTypeName = resultTypeName;
    }

    public List<DomainFunctionArgumentDefinitionImpl> getArgumentDefinitions() {
        return argumentDefinitions;
    }

    public DomainTypeDefinition getResultTypeDefinition() {
        return resultTypeDefinition;
    }

    public void bindTypes(DomainBuilderImpl domainBuilder, MetamodelBuildingContext context) {
        this.function = null;
        if (resultTypeName == null) {
            resultTypeDefinition = null;
        } else {
            resultTypeDefinition = domainBuilder.getDomainTypeDefinition(resultTypeName);
            if (resultTypeDefinition == null) {
                context.addError("The result type '" + resultTypeName + "' defined for the function " + name + " is unknown!");
            }
        }
        if (argumentTypeNames.size() == 0) {
            argumentDefinitions = Collections.emptyList();
        } else {
            argumentDefinitions = new ArrayList<>(argumentTypeNames.size());
            for (int i = 0; i < argumentTypeNames.size(); i++) {
                DomainFunctionArgumentDefinitionImpl domainFunctionArgumentDefinition = new DomainFunctionArgumentDefinitionImpl(this, name, i, argumentTypeNames.get(i));
                domainFunctionArgumentDefinition.bindTypes(domainBuilder, context);
                argumentDefinitions.add(domainFunctionArgumentDefinition);
            }
        }
    }

    public DomainFunction getFunction(MetamodelBuildingContext context) {
        if (function == null) {
            function = new DomainFunctionImpl(this, context);
        }

        return function;
    }
}
