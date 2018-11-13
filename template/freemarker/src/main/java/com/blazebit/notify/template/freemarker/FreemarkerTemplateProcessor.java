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
import com.blazebit.notify.template.api.exception.TemplateProcessorException;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;

public class FreemarkerTemplateProcessor<R extends NotificationReceiver> implements TemplateProcessor<FreemarkerTemplate, R> {
    @Override
    public NotificationMessagePart processTemplate(FreemarkerTemplate template, R receiver) {
        StringWriter stringWriter = new StringWriter();
        try {
            template.getFreemarkerTemplate().process(receiver, stringWriter);
        } catch (TemplateException | IOException e) {
            throw new TemplateProcessorException(e);
        }
        return new DefaultNotificationMessagePart(stringWriter.toString());
    }
}
