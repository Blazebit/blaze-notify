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

package com.blazebit.notify.expression.persistence.function;

import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.runtime.model.*;
import com.blazebit.notify.expression.ExpressionInterpreter;
import com.blazebit.notify.expression.spi.FunctionInvoker;
import com.blazebit.notify.expression.spi.FunctionRenderer;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import static com.blazebit.notify.expression.persistence.PersistenceDomainContributor.INTEGER;

public class SizeFunction implements FunctionRenderer, FunctionInvoker {

    private static final SizeFunction INSTANCE = new SizeFunction();

    private SizeFunction() {
    }

    public static void addFunction(DomainBuilder domainBuilder) {
        domainBuilder.createFunction("SIZE")
                .withMetadata(new FunctionRendererMetadataDefinition(INSTANCE))
                .withMetadata(new FunctionInvokerMetadataDefinition(INSTANCE))
                .withExactArgumentCount(1)
                .withCollectionArgument("collection")
                .withResultType(INTEGER)
                .build();
        domainBuilder.withFunctionTypeResolver("SIZE", new DomainFunctionTypeResolver() {
            @Override
            public DomainType resolveType(DomainModel domainModel, DomainFunction function, Map<DomainFunctionArgument, DomainType> argumentTypes) {
                DomainType argumentType = argumentTypes.values().iterator().next();
                if (!(argumentType instanceof CollectionDomainType)) {
                    throw new IllegalArgumentException("SIZE only accepts a collection argument! Invalid type given: " + argumentType);
                }
                return domainModel.getType(INTEGER);
            }
        });
    }

    @Override
    public Object invoke(ExpressionInterpreter.Context context, DomainFunction function, Map<DomainFunctionArgument, Object> arguments) {
        Object argument = arguments.get(function.getArgument(0));
        if (argument == null) {
            return null;
        }

        if (argument instanceof Collection<?>) {
            return ((Collection) argument).size();
        } else {
            throw new IllegalArgumentException("Illegal argument for SIZE function: " + argument);
        }
    }

    @Override
    public void render(DomainFunction function, DomainType returnType, Map<DomainFunctionArgument, Consumer<StringBuilder>> argumentRenderers, StringBuilder sb) {
        sb.append("SIZE(");
        argumentRenderers.values().iterator().next().accept(sb);
        sb.append(')');
    }
}
