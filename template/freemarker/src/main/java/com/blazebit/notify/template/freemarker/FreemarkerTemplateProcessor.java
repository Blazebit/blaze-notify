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
package com.blazebit.notify.template.freemarker;

import com.blazebit.notify.template.api.ConfigurationSource;
import com.blazebit.notify.template.api.TemplateException;
import com.blazebit.notify.template.api.TemplateProcessor;
import com.blazebit.notify.template.api.TemplateProcessorKey;
import com.blazebit.notify.template.api.TemplateResourceBundleLookup;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * A Freemarker based implementation of a template processor.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class FreemarkerTemplateProcessor implements TemplateProcessor<String>, Serializable {

    /**
     * The key for which the template processor is registered.
     */
    public static final TemplateProcessorKey<String> KEY = TemplateProcessorKey.of("freemarker", String.class);
    /**
     * The configuration property for the Freemarker {@link Configuration} object.
     */
    public static final String FREEMARKER_CONFIGURATION_PROPERTY = "configuration";
    /**
     * The configuration property for the Freemarker template encoding.
     */
    public static final String FREEMARKER_ENCODING_PROPERTY = "encoding";
    /**
     * The configuration property for the Freemarker {@link Template}.
     */
    public static final String FREEMARKER_TEMPLATE_PROPERTY = "template";

    /**
     * The configuration property for the {@link ResourceBundle}.
     */
    public static final String RESOURCE_BUNDLE_KEY = "resourceBundle";
    /**
     * The configuration property for the {@link Locale}.
     */
    public static final String LOCALE_KEY = "locale";

    private final FreemarkerTemplateLookup freemarkerTemplateLookup;

    private final TemplateResourceBundleLookup resourceBundleLookup;

    /**
     * Creates a new Freemarker template processor from the given configuration source.
     *
     * @param templateName The template name
     * @param configurationSource The configuration source
     */
    public FreemarkerTemplateProcessor(String templateName, ConfigurationSource configurationSource) {
        Function<String, FreemarkerTemplateLookup> templateAccessor = name -> (Locale locale) -> {
            Configuration configuration = configurationSource.getPropertyOrDefault(FREEMARKER_CONFIGURATION_PROPERTY, Configuration.class, null, o -> {
                Configuration c = new Configuration(Configuration.VERSION_2_3_28);
                c.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "");
                return c;
            });
            String templateEncoding = configurationSource.getPropertyOrDefault(FREEMARKER_ENCODING_PROPERTY, String.class, Function.identity(), o -> null);
            try {
                return configuration.getTemplate(name, locale, templateEncoding);
            } catch (IOException e) {
                throw new TemplateException("", e);
            }
        };
        this.freemarkerTemplateLookup = configurationSource.getPropertyOrDefault(FREEMARKER_TEMPLATE_PROPERTY, FreemarkerTemplateLookup.class, templateAccessor, o -> templateAccessor.apply(templateName));
        Function<String, TemplateResourceBundleLookup> resourceBundleAccessor = name -> (Locale locale) -> ResourceBundle.getBundle(name, locale);
        this.resourceBundleLookup = configurationSource.getPropertyOrDefault(RESOURCE_BUNDLE_KEY, TemplateResourceBundleLookup.class, resourceBundleAccessor, o -> locale -> null);
    }

    /**
     * Creates a new Freemarker template processor from the given template.
     *
     * @param freemarkerTemplateLookup The template
     * @param resourceBundle The resource bundle
     */
    public FreemarkerTemplateProcessor(Template freemarkerTemplateLookup, ResourceBundle resourceBundle) {
        this.freemarkerTemplateLookup = locale -> freemarkerTemplateLookup;
        this.resourceBundleLookup = locale -> resourceBundle;
    }

    /**
     * Creates a new Freemarker template processor from the given locale aware template function.
     *
     * @param freemarkerTemplateLookup The locale aware template function
     * @param resourceBundleLookup The locale aware resource bundle function
     */
    public FreemarkerTemplateProcessor(FreemarkerTemplateLookup freemarkerTemplateLookup, TemplateResourceBundleLookup resourceBundleLookup) {
        this.freemarkerTemplateLookup = freemarkerTemplateLookup;
        this.resourceBundleLookup = resourceBundleLookup;
    }

    @Override
    public String processTemplate(Map<String, Object> model) {
        Locale locale = (Locale) model.get(LOCALE_KEY);
        ResourceBundle resourceBundle = resourceBundleLookup.findResourceBundle(locale);
        if (resourceBundle == null) {
            resourceBundle = (ResourceBundle) model.get(RESOURCE_BUNDLE_KEY);
        }
        if (resourceBundle != null) {
            model = new HashMap<>(model);
            if (locale == null) {
                locale = resourceBundle.getLocale();
            }
            model.put("msg", new MessageFormatterMethod(locale, resourceBundle));
        }

        StringWriter stringWriter = new StringWriter();
        try {
            freemarkerTemplateLookup.findTemplate(locale).process(model, stringWriter);
        } catch (freemarker.template.TemplateException | IOException e) {
            throw new TemplateException(e);
        }
        return stringWriter.toString();
    }
}
