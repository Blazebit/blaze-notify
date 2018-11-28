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

import com.blazebit.notify.notification.DefaultNotificationMessagePart;
import com.blazebit.notify.notification.NotificationMessagePart;
import com.blazebit.notify.notification.NotificationReceiver;
import com.blazebit.notify.template.api.TemplateProcessor;
import com.blazebit.notify.template.api.TemplateProcessorException;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public abstract class FreemarkerTemplateProcessor<R extends NotificationReceiver, P extends NotificationMessagePart> implements TemplateProcessor<FreemarkerTemplate, R, P> {
    @Override
    public P processTemplate(FreemarkerTemplate template, Map<String, Object> model) {
        StringWriter stringWriter = new StringWriter();
        try {
            template.getFreemarkerTemplate().process(model, stringWriter);
        } catch (TemplateException | IOException e) {
            throw new TemplateProcessorException(e);
        }
        return createNotificationMessagePart(stringWriter.toString());
    }

    protected abstract P createNotificationMessagePart(String processedTemplateContent);
}
