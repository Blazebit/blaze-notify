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

package com.blazebit.notify.expression.spi;

import com.blazebit.notify.domain.runtime.model.DomainFunction;
import com.blazebit.notify.domain.runtime.model.DomainFunctionArgument;
import com.blazebit.notify.domain.runtime.model.DomainType;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface FunctionRenderer {

    void render(DomainFunction function, DomainType returnType, Map<DomainFunctionArgument, Consumer<StringBuilder>> argumentRenderers, StringBuilder sb);

    static FunctionRenderer builtin(String persistenceFunctionName) {
        return (function, returnType, argumentRenderers, sb) -> {
            sb.append(persistenceFunctionName).append(", ");
            if (!argumentRenderers.isEmpty()) {
                for (Consumer<StringBuilder> value : argumentRenderers.values()) {
                    value.accept(sb);
                    sb.append(", ");
                }
                sb.setLength(sb.length() - 2);
            }
            sb.append(')');
        };
    }

    static FunctionRenderer function(String persistenceFunctionName) {
        return (function, returnType, argumentRenderers, sb) -> {
            sb.append("FUNCTION('").append(persistenceFunctionName).append("', ");
            if (!argumentRenderers.isEmpty()) {
                for (Consumer<StringBuilder> value : argumentRenderers.values()) {
                    value.accept(sb);
                    sb.append(", ");
                }
                sb.setLength(sb.length() - 2);
            }
            sb.append(')');
        };
    }

}
