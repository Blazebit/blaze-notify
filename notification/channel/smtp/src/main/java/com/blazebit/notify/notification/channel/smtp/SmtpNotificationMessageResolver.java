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
package com.blazebit.notify.notification.channel.smtp;

import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.NotificationJob;
import com.blazebit.notify.notification.NotificationMessageResolver;
import com.blazebit.notify.template.api.Template;
import com.blazebit.notify.template.api.TemplateLoader;
import com.blazebit.notify.template.api.TemplateProcessor;
import com.blazebit.notify.template.api.TemplateProcessorRegistry;

import java.util.*;

public class SmtpNotificationMessageResolver<R extends SmtpNotificationRecipient, N extends Notification<R, N, SmtpMessage>> implements NotificationMessageResolver<R, N, SmtpMessage> {

    private final String from;
    private final String fromDisplayName;
    private final String replyTo;
    private final String replyToDisplayName;
    private final String envelopeFrom;
    private final String resourceBundle;
    private final TemplateLoader<R> subjectTemplateLoader;
    private final TemplateLoader<R> textBodyTemplateLoader;
    private final TemplateLoader<R> htmlBodyTemplateLoader;
    private final Collection<TemplateLoader<R>> attachmentLoaders;
    private final TemplateProcessorRegistry templateProcessorRegistry;

    public SmtpNotificationMessageResolver(String from, String fromDisplayName, String replyTo, String replyToDisplayName, String envelopeFrom, String resourceBundle, TemplateLoader<R> subjectTemplateLoader, TemplateLoader<R> textBodyTemplateLoader, TemplateLoader<R> htmlBodyTemplateLoader, Collection<TemplateLoader<R>> attachmentLoaders, TemplateProcessorRegistry templateProcessorRegistry) {
        this.from = from;
        this.fromDisplayName = fromDisplayName;
        this.replyTo = replyTo;
        this.replyToDisplayName = replyToDisplayName;
        this.envelopeFrom = envelopeFrom;
        this.resourceBundle = resourceBundle;
        this.subjectTemplateLoader = subjectTemplateLoader;
        this.textBodyTemplateLoader = textBodyTemplateLoader;
        this.htmlBodyTemplateLoader = htmlBodyTemplateLoader;
        this.attachmentLoaders = attachmentLoaders == null ? Collections.<TemplateLoader<R>>emptyList() : attachmentLoaders;
        this.templateProcessorRegistry = templateProcessorRegistry;
    }

    @Override
    public SmtpMessage resolveNotificationMessage(NotificationJob<R, N, SmtpMessage> notificationJob, R notificationRecipient) {
        Map<String, Object> resolvedJobParameters = new HashMap<>(notificationJob.getJobParameters());
        Locale locale = notificationRecipient.getLocale();
        if (resourceBundle != null) {
            ResourceBundle resourceBundle = ResourceBundle.getBundle(this.resourceBundle, locale);
            resolvedJobParameters.put("resourceBundle", resourceBundle);
        }
        resolvedJobParameters.put("locale", locale);
        resolvedJobParameters.put("recipient", notificationRecipient);
        resolvedJobParameters = Collections.unmodifiableMap(resolvedJobParameters);

        EmailSubject subject = subjectTemplateLoader == null ? null : new EmailSubject(loadAndprocessTemplate(subjectTemplateLoader, notificationRecipient, resolvedJobParameters, String.class));
        EmailBody textBody = textBodyTemplateLoader == null ? null : new EmailBody(loadAndprocessTemplate(textBodyTemplateLoader, notificationRecipient, resolvedJobParameters, String.class));
        EmailBody htmlBody = htmlBodyTemplateLoader == null ? null : new EmailBody(loadAndprocessTemplate(htmlBodyTemplateLoader, notificationRecipient, resolvedJobParameters, String.class));
        Collection<Attachment> attachments = new ArrayList<>(attachmentLoaders.size());
        for (TemplateLoader<R> attachmentTemplateLoader : attachmentLoaders) {
            attachments.add(loadAndprocessTemplate(attachmentTemplateLoader, notificationRecipient, resolvedJobParameters, Attachment.class));
        }
        return new SmtpMessage(from, fromDisplayName, replyTo, replyToDisplayName, envelopeFrom, subject, textBody, htmlBody, attachments);
    }

    private <D> D loadAndprocessTemplate(TemplateLoader<R> templateLoader, R notificationRecipient, Map<String, Object> model, Class<D> expectedTemplateProcessingResultType) {
        Template template = templateLoader.loadTemplate(notificationRecipient);
        TemplateProcessor<Template, D> templateProcessor = (TemplateProcessor<Template, D>) templateProcessorRegistry.getTemplateProcessor(template.getClass(), expectedTemplateProcessingResultType);
        if (templateProcessor == null) {
            throw new RuntimeException(
                    String.format("No template processor found for template of type %s with expected processing result type %s",
                    template.getClass(),
                    expectedTemplateProcessingResultType)
            );
        }
        return templateProcessor.processTemplate(template, model);
    }
}
