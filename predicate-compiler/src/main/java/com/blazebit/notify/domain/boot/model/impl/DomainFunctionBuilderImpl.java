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

import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.boot.model.DomainFunctionBuilder;

import java.util.Arrays;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DomainFunctionBuilderImpl implements DomainFunctionBuilder {

    private final DomainBuilderImpl domainBuilder;
    private final DomainFunctionDefinition domainFunctionDefinition;

    public DomainFunctionBuilderImpl(DomainBuilderImpl domainBuilder, String name) {
        this.domainBuilder = domainBuilder;
        this.domainFunctionDefinition = new DomainFunctionDefinition(name);
    }

    @Override
    public DomainFunctionBuilder withMinArgumentCount(int minArgumentCount) {
        domainFunctionDefinition.setMinArgumentCount(minArgumentCount);
        return this;
    }

    @Override
    public DomainFunctionBuilder withExactArgumentCount(int exactArgumentCount) {
        domainFunctionDefinition.setArgumentCount(exactArgumentCount);
        return this;
    }

    @Override
    public DomainFunctionBuilder withArgument(String name, String typeName) {
        if (domainFunctionDefinition.getArgumentNames().size() != domainFunctionDefinition.getArgumentTypeNames().size()) {
            throw new IllegalArgumentException("Can't mix positional and named parameters!");
        }
        domainFunctionDefinition.getArgumentNames().add(name);
        domainFunctionDefinition.getArgumentTypeNames().add(typeName);
        return this;
    }

    @Override
    public DomainFunctionBuilder withArgumentTypes(String... typeNames) {
        if (domainFunctionDefinition.getArgumentNames().size() != domainFunctionDefinition.getArgumentTypeNames().size()) {
            throw new IllegalArgumentException("Can't mix positional and named parameters!");
        }
        domainFunctionDefinition.setArgumentTypeNames(Arrays.asList(typeNames));
        return this;
    }

    @Override
    public DomainFunctionBuilder withResultType(String typeName) {
        domainFunctionDefinition.setResultTypeName(typeName);
        return this;
    }

    @Override
    public DomainBuilder build() {
        return domainBuilder.withDomainFunctionDefinition(domainFunctionDefinition);
    }
}
