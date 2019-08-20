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
import com.blazebit.notify.domain.runtime.model.DomainFunction;
import com.blazebit.notify.domain.runtime.model.DomainFunctionArgument;
import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.expression.ExpressionInterpreter;
import com.blazebit.notify.expression.spi.FunctionInvoker;
import com.blazebit.notify.expression.spi.FunctionRenderer;

import java.util.Map;
import java.util.function.Consumer;

import static com.blazebit.notify.expression.persistence.PersistenceDomainContributor.STRING;

public class TrimFunction implements FunctionRenderer, FunctionInvoker {

    private static final TrimFunction INSTANCE = new TrimFunction();

    private TrimFunction() {
    }

    public static void addFunction(DomainBuilder domainBuilder) {
        domainBuilder.createFunction("TRIM")
                .withMetadata(new FunctionRendererMetadataDefinition(INSTANCE))
                .withMetadata(new FunctionInvokerMetadataDefinition(INSTANCE))
                .withMinArgumentCount(1)
                .withResultType(STRING)
                .withArgument("string", STRING)
                .withArgument("character", STRING)
                .build();
    }

    @Override
    public Object invoke(ExpressionInterpreter.Context context, DomainFunction function, Map<DomainFunctionArgument, Object> arguments) {
        Object string = arguments.get(function.getArgument(0));
        if (string == null) {
            return null;
        }
        Object character = arguments.getOrDefault(function.getArgument(1), ' ');
        if (character == null) {
            return null;
        }

        String s = string.toString();
        char c = (char) character;
        int start = 0;
        int end = s.length() - 1;
        for (; start < s.length(); start++) {
            if (c != s.charAt(start)) {
                break;
            }
        }
        for (; start < end; end--) {
            if (c != s.charAt(end)) {
                break;
            }
        }

        return s.substring(start, end);
    }

    @Override
    public void render(DomainFunction function, DomainType returnType, Map<DomainFunctionArgument, Consumer<StringBuilder>> argumentRenderers, StringBuilder sb) {
        sb.append("TRIM(");
        argumentRenderers.get(function.getArgument(0)).accept(sb);
        Consumer<StringBuilder> secondArg = argumentRenderers.get(function.getArgument(1));
        if (secondArg != null) {
            sb.append(", ");
            secondArg.accept(sb);
        }
        sb.append(')');
    }
}