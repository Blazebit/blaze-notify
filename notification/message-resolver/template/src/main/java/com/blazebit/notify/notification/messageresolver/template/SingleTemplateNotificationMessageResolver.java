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
package com.blazebit.notify.notification.messageresolver.template;

import com.blazebit.notify.notification.*;
import com.blazebit.notify.template.api.Template;
import com.blazebit.notify.template.api.TemplateLoader;
import com.blazebit.notify.template.api.TemplateProcessor;

public abstract class SingleTemplateNotificationMessageResolver<R extends NotificationReceiver, M extends NotificationMessage, T extends Template> implements NotificationMessageResolver<R, M> {

    private final TemplateLoader<T> templateLoader;
    private final TemplateProcessor<T, R> templateProcessor;
    private T cachedTemplate;

    protected SingleTemplateNotificationMessageResolver(TemplateLoader<T> templateLoader, TemplateProcessor<T, R> templateProcessor) {
        this.templateLoader = templateLoader;
        this.templateProcessor = templateProcessor;
    }

    @Override
    public M create(R notificationReceiver, NotificationJobContext context) {
        if (cachedTemplate == null) {
            this.cachedTemplate = templateLoader.loadTemplate();
        }
        return createNotificationMessage(templateProcessor.processTemplate(cachedTemplate, notificationReceiver));
    }

    protected abstract M createNotificationMessage(NotificationMessagePart part);
}
