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
import com.blazebit.notify.domain.persistence.StaticDomainFunctionTypeResolvers;
import com.blazebit.notify.domain.runtime.model.DomainFunction;
import com.blazebit.notify.domain.runtime.model.DomainFunctionArgument;
import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.expression.ExpressionInterpreter;
import com.blazebit.notify.expression.spi.FunctionInvoker;
import com.blazebit.notify.expression.spi.FunctionRenderer;

import java.util.Map;
import java.util.function.Consumer;

import static com.blazebit.notify.expression.persistence.PersistenceDomainContributor.NUMERIC;
import static com.blazebit.notify.expression.persistence.PersistenceDomainContributor.STRING;

public class GreatestFunction implements FunctionRenderer, FunctionInvoker {

    private static final GreatestFunction INSTANCE = new GreatestFunction();

    private GreatestFunction() {
    }

    public static void addFunction(DomainBuilder domainBuilder) {
        domainBuilder.createFunction("GREATEST")
                .withMetadata(new FunctionRendererMetadataDefinition(INSTANCE))
                .withMetadata(new FunctionInvokerMetadataDefinition(INSTANCE))
                .withMinArgumentCount(2)
                .build();
        domainBuilder.withFunctionTypeResolver("GREATEST", StaticDomainFunctionTypeResolvers.widest(NUMERIC));
    }

    @Override
    public Object invoke(ExpressionInterpreter.Context context, DomainFunction function, Map<DomainFunctionArgument, Object> arguments) {
        Comparable greatest = null;
        for (Object value : arguments.values()) {
            // TODO: automatic widening of arguments?
            if (greatest == null || greatest.compareTo(value) < 0) {
                greatest = (Comparable) value;
            }
        }
        return greatest;
    }

    @Override
    public void render(DomainFunction function, DomainType returnType, Map<DomainFunctionArgument, Consumer<StringBuilder>> argumentRenderers, StringBuilder sb) {
        sb.append("GREATEST(");
        for (Consumer<StringBuilder> consumer : argumentRenderers.values()) {
            sb.append(", ");
            consumer.accept(sb);
        }

        sb.setLength(sb.length() - 2);
        sb.append(')');
    }
}
