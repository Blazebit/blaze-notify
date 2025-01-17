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
package com.blazebit.template.thymeleaf;

import com.blazebit.job.ServiceProvider;
import com.blazebit.notify.template.api.TemplateException;
import com.blazebit.notify.template.api.TemplateProcessor;
import com.blazebit.notify.template.api.TemplateProcessorKey;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

/**
 * A Thymeleaf based implementation of a template processor.
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ThymeleafTemplateProcessor implements TemplateProcessor<String>, Serializable {

    public static final TemplateProcessorKey<String> KEY = TemplateProcessorKey.of("thymeleaf", String.class);

    public static final String LOCALE_MODEL_KEY = "locale";

    private final ITemplateEngine templateEngine;
    private final String templateName;

    /**
     * Creates a new Thymeleaf template processor from the given configuration source.
     *
     * @param templateName The template name
     * @param serviceProvider The service provider of the job context
     *
     */
    public ThymeleafTemplateProcessor(String templateName, ServiceProvider serviceProvider) {
        ITemplateEngine templateEngine = serviceProvider.getService(ITemplateEngine.class);
        this.templateEngine = templateEngine == null ? new TemplateEngine() : templateEngine;
        this.templateName = templateName;
    }

    @Override
    public String processTemplate(Map<String, Object> model) {
        Locale locale = (Locale) model.get(LOCALE_MODEL_KEY);
        if (locale == null) {
            locale = Locale.getDefault();
        }
        IContext context = new Context(locale, model);
        try {
            return templateEngine.process(templateName, context);
        } catch (RuntimeException e) {
            throw new TemplateException(e);
        }
    }
}
