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

package com.blazebit.notify.expression.declarative;

import com.blazebit.notify.domain.boot.model.MetadataDefinition;
import com.blazebit.notify.domain.boot.model.MetadataDefinitionHolder;
import com.blazebit.notify.domain.runtime.model.DomainFunction;
import com.blazebit.notify.domain.runtime.model.DomainFunctionArgument;
import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.expression.spi.FunctionRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ExpressionFunctionRenderer implements MetadataDefinition<FunctionRenderer>, FunctionRenderer {

    private final String[] chunks;
    private final Integer[] parameterIndices;

    public ExpressionFunctionRenderer(String template) {
        List<String> chunkList = new ArrayList<String>();
        List<Integer> parameterIndexList = new ArrayList<Integer>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);

            if (c == '?') {
                chunkList.add(sb.toString());
                sb.setLength(0);

                while (++i < template.length()) {
                    c = template.charAt(i);
                    if (Character.isDigit(c)) {
                        sb.append(c);
                    } else {
                        parameterIndexList.add(Integer.valueOf(sb.toString()) - 1);
                        sb.setLength(0);
                        sb.append(c);
                        break;
                    }
                }

                if (i == template.length()) {
                    parameterIndexList.add(Integer.valueOf(sb.toString()) - 1);
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }

        if (sb.length() > 0) {
            chunkList.add(sb.toString());
        }

        this.chunks = chunkList.toArray(new String[chunkList.size()]);
        this.parameterIndices = parameterIndexList.toArray(new Integer[parameterIndexList.size()]);
    }

    @Override
    public void render(DomainFunction function, DomainType returnType, Map<DomainFunctionArgument, Consumer<StringBuilder>> argumentRenderers, StringBuilder sb) {
        for (int i = 0; i < chunks.length; i++) {
            sb.append(chunks[i]);

            if (i < parameterIndices.length) {
                argumentRenderers.get(function.getArgument(parameterIndices[i])).accept(sb);
            }
        }

    }

    @Override
    public Class<FunctionRenderer> getJavaType() {
        return FunctionRenderer.class;
    }

    @Override
    public FunctionRenderer build(MetadataDefinitionHolder<?> definitionHolder) {
        return this;
    }
}
