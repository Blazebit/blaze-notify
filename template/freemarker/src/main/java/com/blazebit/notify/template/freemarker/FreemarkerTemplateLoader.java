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

import com.blazebit.notify.notification.NotificationJobProcessingContext;
import com.blazebit.notify.notification.NotificationReceiver;
import com.blazebit.notify.template.api.TemplateLoader;
import freemarker.template.Configuration;

import java.io.IOException;
import java.util.Locale;

public class FreemarkerTemplateLoader<R extends NotificationReceiver> implements TemplateLoader<R, FreemarkerTemplate> {

    private final Configuration cfg;
    private final String templateName;
    private final String templateEncoding;

    public FreemarkerTemplateLoader(Configuration cfg, String templateName) {
        this(cfg, templateName, null);
    }

    public FreemarkerTemplateLoader(Configuration cfg, String templateName, String templateEncoding) {
        this.cfg = cfg;
        this.templateName = templateName;
        this.templateEncoding = templateEncoding;
    }

    @Override
    public FreemarkerTemplate loadTemplate(R notificationReceiver, NotificationJobProcessingContext context) {
        Locale locale = resolveReceiverLocale(notificationReceiver);
        freemarker.template.Template template;
        try {
            template = locale == null ? cfg.getTemplate(templateName, templateEncoding) : cfg.getTemplate(templateName, locale, templateEncoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FreemarkerTemplate(template);
    }

    protected Locale resolveReceiverLocale(R notificationReceiver) {
        return null;
    }
}
