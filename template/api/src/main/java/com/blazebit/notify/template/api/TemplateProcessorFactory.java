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

import com.blazebit.job.ServiceProvider;

/**
 * A factory for template processors of a specific type.
 *
 * @param <R> The template processor result type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface TemplateProcessorFactory<R> {

    /**
     * Returns the template processor key class.
     *
     * @return the template processor key class
     */
    TemplateProcessorKey<R> getTemplateProcessorKey();

    /**
     * Creates a new template processor for the given template context and configuration source.
     *
     * @param templateContext The template context
     * @param templateName The name of the template to retrieve a template processor for
     * @param configurationSource The configuration source
     * @param serviceProvider The service provider of the job context
     * @return the template processor
     */
    TemplateProcessor<R> createTemplateProcessor(TemplateContext templateContext, String templateName, ConfigurationSource configurationSource, ServiceProvider serviceProvider);
}
