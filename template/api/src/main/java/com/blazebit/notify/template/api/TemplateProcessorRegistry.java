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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class TemplateProcessorRegistry {

    private static final Map<String, TemplateProcessorFactory> templateProcessorFactories = new HashMap<>();
    private final Map<String, TemplateProcessor<?>> templateProcessors = new HashMap<>();

    static {
        ServiceLoader<TemplateProcessorFactory> serviceLoader = ServiceLoader.load(TemplateProcessorFactory.class);
        for (TemplateProcessorFactory templateProcessorFactory : serviceLoader) {
            templateProcessorFactories.put(templateProcessorFactory.getTemplateType(), templateProcessorFactory);
        }
    }

    public TemplateProcessor<?> getTemplateProcessor(String templateType) {
        TemplateProcessor<?> templateProcessor = templateProcessors.get(templateType);
        if (templateProcessor == null) {
            TemplateProcessorFactory templateProcessorFactory = templateProcessorFactories.get(templateType);
            if (templateProcessorFactory != null) {
                templateProcessor = templateProcessorFactory.createTemplateProcessor();
                templateProcessors.put(templateType, templateProcessor);
            }
        }
        return templateProcessor;
    }
}
