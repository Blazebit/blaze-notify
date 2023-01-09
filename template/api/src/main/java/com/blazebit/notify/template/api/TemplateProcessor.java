/*
 * Copyright 2018 - 2023 Blazebit.
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
package com.blazebit.notify.template.api;

import java.io.Serializable;
import java.util.Map;

/**
 * A template processor that can be applied on a map model to produce a result.
 *
 * @param <R> The template processor result type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface TemplateProcessor<R> extends Serializable {

    String TEMPLATE_NAME_PROPERTY = "template";

    /**
     * Processes this template based on the given map model.
     *
     * @param model The model
     * @return The result
     */
    R processTemplate(Map<String, Object> model);

    /**
     * Returns a {@link TemplateProcessor} that statically always processes the given element.
     *
     * @param element The template processor result to resolve statically
     * @param <T>     The template processor result type
     * @return a {@link TemplateProcessor}
     */
    static <T extends Serializable> TemplateProcessor<T> of(T element) {
        return new TemplateProcessor<T>() {
            @Override
            public T processTemplate(Map<String, Object> model) {
                return element;
            }
        };
    }
}
