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
package com.blazebit.notify.template.freemarker;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.notify.template.api.*;

@ServiceProvider(TemplateProcessorFactory.class)
public class FreemarkerTemplateProcessorFactory implements TemplateProcessorFactory<String> {

    @Override
    public TemplateProcessorKey<String> getTemplateProcessorKey() {
        return FreemarkerTemplateProcessor.KEY;
    }

    @Override
    public TemplateProcessor<String> createTemplateProcessor(TemplateContext templateContext, ConfigurationSource configurationSource) {
        return new FreemarkerTemplateProcessor(configurationSource);
    }
}
