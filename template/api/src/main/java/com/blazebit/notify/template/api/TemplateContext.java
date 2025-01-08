/*
 * Copyright 2018 - 2025 Blazebit.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * A context for template processor factories.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface TemplateContext {

    /**
     * Returns a {@link TemplateProcessorFactory} for the given template processor type and the given result type.
     *
     * @param type       The template processor type identifier
     * @param resultType The result type class
     * @param <T>        The result type
     * @return a {@link TemplateProcessorFactory}
     */
    <T> TemplateProcessorFactory<T> getTemplateProcessorFactory(String type, Class<T> resultType);

    /**
     * Returns a {@link TemplateProcessorFactory} for the given template processor key.
     *
     * @param key The template processor key
     * @param <T> The result type
     * @return a {@link TemplateProcessorFactory}
     */
    <T> TemplateProcessorFactory<T> getTemplateProcessorFactory(TemplateProcessorKey<T> key);

    /**
     * Returns a builder for a template context.
     *
     * @return a builder for a template context
     */
    static Builder builder() {
        Builder builder = new Builder();
        builder.loadDefaults();
        return builder;
    }

    /**
     * A builder for a template context.
     *
     * @author Christian Beikov
     * @since 1.0.0
     */
    static class Builder {

        private final Set<TemplateProcessorFactory<?>> templateProcessorFactories = new HashSet<>();

        private void loadDefaults() {
            for (TemplateProcessorFactory templateProcessorFactory : ServiceLoader.load(TemplateProcessorFactory.class)) {
                templateProcessorFactories.add(templateProcessorFactory);
            }
        }

        /**
         * Returns the registered template processor factories.
         *
         * @return The registered template processor factories
         */
        public Set<TemplateProcessorFactory<?>> getTemplateProcessorFactories() {
            return templateProcessorFactories;
        }

        /**
         * Adds the given template processor factory.
         *
         * @param templateProcessorFactory The template processor factory
         * @return this for chaining
         */
        public Builder withTemplateProcessorFactory(TemplateProcessorFactory<?> templateProcessorFactory) {
            templateProcessorFactories.add(templateProcessorFactory);
            return this;
        }

        /**
         * Returns a new template context.
         *
         * @return a new template context
         */
        public TemplateContext createContext() {
            return new DefaultContext(templateProcessorFactories);
        }

        /**
         * A basic implementation of the {@link TemplateContext} interface.
         *
         * @author Christian Beikov
         * @since 1.0.0
         */
        private static class DefaultContext implements TemplateContext {

            private final Map<Class<?>, Map<String, TemplateProcessorFactory<?>>> templateProcessorFactories;

            private DefaultContext(Collection<TemplateProcessorFactory<?>> templateProcessorFactories) {
                Map<Class<?>, Map<String, TemplateProcessorFactory<?>>> map = new HashMap<>();
                for (TemplateProcessorFactory<?> templateProcessorFactory : templateProcessorFactories) {
                    TemplateProcessorKey<?> templateProcessorKey = templateProcessorFactory.getTemplateProcessorKey();
                    map.computeIfAbsent(templateProcessorKey.getTemplateProcessorResultType(), k -> new HashMap<>())
                        .put(templateProcessorKey.getTemplateProcessorType(), templateProcessorFactory);
                }

                this.templateProcessorFactories = map;
            }

            @Override
            public <T> TemplateProcessorFactory<T> getTemplateProcessorFactory(String type, Class<T> resultType) {
                Map<String, TemplateProcessorFactory<?>> map = templateProcessorFactories.get(resultType);
                if (map == null) {
                    return null;
                }
                return (TemplateProcessorFactory<T>) map.get(type);
            }

            @Override
            public <T> TemplateProcessorFactory<T> getTemplateProcessorFactory(TemplateProcessorKey<T> key) {
                return getTemplateProcessorFactory(key.getTemplateProcessorType(), key.getTemplateProcessorResultType());
            }
        }
    }
}
