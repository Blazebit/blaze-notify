/*
 * Copyright 2018 - 2020 Blazebit.
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

/**
 * A type safe wrapper for identifying template processors by their template type.
 *
 * @param <R> The template processor result type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface TemplateProcessorKey<R> {

    /**
     * Returns the template processor type identifier.
     *
     * @return the template processor type identifier
     */
    String getTemplateProcessorType();

    /**
     * Returns the template processor result class.
     *
     * @return the template processor result class
     */
    Class<R> getTemplateProcessorResultType();

    /**
     * Returns a new template processor key for the given type identifier and the given class.
     *
     * @param type  The template processor type identifier
     * @param clazz The template processor result class
     * @param <T>   The template processor result type
     * @return A new template processor key
     */
    static <T> TemplateProcessorKey<T> of(String type, Class<T> clazz) {
        return new TemplateProcessorKey<T>() {
            @Override
            public String getTemplateProcessorType() {
                return type;
            }

            @Override
            public Class<T> getTemplateProcessorResultType() {
                return clazz;
            }
        };
    }
}
