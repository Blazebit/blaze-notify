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

public class TemplateProcessorRegistry {

    private static final Collection<TemplateProcessorFactory<?>> templateProcessorFactories = new HashSet<>();
    private final Map<TemplateProcessorCacheKey, TemplateProcessor<?, ?>> cachedTemplateProcessors = new HashMap<>();

    static {
        ServiceLoader<TemplateProcessorFactory> serviceLoader = ServiceLoader.load(TemplateProcessorFactory.class);
        for (TemplateProcessorFactory templateProcessorFactory : serviceLoader) {
            templateProcessorFactories.add(templateProcessorFactory);
        }
    }

    public <T extends Template, R> TemplateProcessor<T, R> getTemplateProcessor(Class<T> templateType, Class<R> expectedTemplateProcessingResultType) {
        TemplateProcessorCacheKey cacheKey = new TemplateProcessorCacheKey(templateType, expectedTemplateProcessingResultType);
        TemplateProcessor<T, R> templateProcessor = (TemplateProcessor<T, R>) cachedTemplateProcessors.get(cacheKey);
        if (templateProcessor == null) {
            for (TemplateProcessorFactory<?> templateProcessorFactory : templateProcessorFactories) {
                if (templateProcessorFactory.canProcessTemplateOfType(templateType, this) &&
                        expectedTemplateProcessingResultType.isAssignableFrom(templateProcessorFactory.getTemplateProcessorResultType())) {
                    if (templateProcessor == null) {
                        templateProcessor = (TemplateProcessor<T, R>) templateProcessorFactory.createTemplateProcessor(this);
                        cachedTemplateProcessors.put(cacheKey, templateProcessor);
                    } else {
                        throw new RuntimeException(
                                String.format("Multiple template process factories for template type %s and result type %s",
                                        templateType,
                                        expectedTemplateProcessingResultType)
                        );
                    }
                }
            }
        }
        return templateProcessor;
    }

    private static class TemplateProcessorCacheKey {
        private final Class<? extends Template> templateClass;
        private final Class<?> processorResultType;

        private TemplateProcessorCacheKey(Class<? extends Template> templateClass, Class<?> processorResultType) {
            this.templateClass = templateClass;
            this.processorResultType = processorResultType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TemplateProcessorCacheKey that = (TemplateProcessorCacheKey) o;
            return Objects.equals(templateClass, that.templateClass) &&
                    Objects.equals(processorResultType, that.processorResultType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(templateClass, processorResultType);
        }
    }
}
