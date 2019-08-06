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
package com.blazebit.notify.template.api;

import java.util.*;

public interface TemplateContext {

    <T> TemplateProcessorFactory<T> getTemplateProcessorFactory(String type, Class<T> resultType);

    <T> TemplateProcessorFactory<T> getTemplateProcessorFactory(TemplateProcessorKey<T> key);

    static class Builder {

        private final Set<TemplateProcessorFactory<?>> templateProcessorFactories = new HashSet<>();

        public static Builder create() {
            Builder builder = new Builder();
            builder.loadDefaults();
            return builder;
        }

        private void loadDefaults() {
            for (TemplateProcessorFactory templateProcessorFactory : ServiceLoader.load(TemplateProcessorFactory.class)) {
                templateProcessorFactories.add(templateProcessorFactory);
            }
        }

        public Set<TemplateProcessorFactory<?>> getTemplateProcessorFactories() {
            return templateProcessorFactories;
        }

        public void withTemplateProcessorFactory(TemplateProcessorFactory<?> templateProcessorFactory) {
            templateProcessorFactories.add(templateProcessorFactory);
        }

        public TemplateContext createContext() {
            return new DefaultContext(templateProcessorFactories);
        }

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
