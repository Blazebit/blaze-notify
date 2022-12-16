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
package com.blazebit.notify.email.message;

import com.blazebit.job.ConfigurationSource;
import com.blazebit.job.ServiceProvider;
import com.blazebit.notify.Notification;
import com.blazebit.notify.NotificationException;
import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.NotificationMessageResolver;
import com.blazebit.notify.NotificationMessageResolverModelCustomizer;
import com.blazebit.notify.NotificationRecipient;
import com.blazebit.notify.template.api.TemplateContext;
import com.blazebit.notify.template.api.TemplateProcessor;
import com.blazebit.notify.template.api.TemplateProcessorFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * A message resolver for E-Mail notification messages.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailNotificationMessageResolver implements NotificationMessageResolver<EmailNotificationMessage> {

    /**
     * The configuration property for the E-Mail from address.
     */
    public static final String EMAIL_MESSAGE_FROM_PROPERTY = "message.email.from";
    /**
     * The configuration property for the E-Mail from display name.
     */
    public static final String EMAIL_MESSAGE_FROM_NAME_PROPERTY = "message.email.from_name";
    /**
     * The configuration property for the E-Mail reply to address.
     */
    public static final String EMAIL_MESSAGE_REPLY_TO_PROPERTY = "message.email.reply_to";
    /**
     * The configuration property for the E-Mail reply to display name.
     */
    public static final String EMAIL_MESSAGE_REPLY_TO_NAME_PROPERTY = "message.email_reply_to_name";
    /**
     * The configuration property for the E-Mail envelop from address.
     */
    public static final String EMAIL_MESSAGE_ENVELOP_FROM_PROPERTY = "message.email.envelop_from";
    /**
     * The configuration property for the E-Mail {@link ResourceBundle}.
     */
    public static final String EMAIL_MESSAGE_RESOURCE_BUNDLE_PROPERTY = "message.email.resource_bundle";
    /**
     * The configuration property for the {@link TemplateContext} to use for the subject and body properties.
     */
    public static final String EMAIL_TEMPLATE_CONTEXT_PROPERTY = "message.email.template_context";
    /**
     * The configuration property for the {@link TemplateProcessorFactory} to use for the subject and body properties.
     */
    public static final String EMAIL_TEMPLATE_PROCESSOR_TYPE_PROPERTY = "message.email.template_processor_type";
    /**
     * The configuration property for the E-Mail subject.
     */
    public static final String EMAIL_MESSAGE_SUBJECT_PROPERTY = "message.email.subject.value";
    /**
     * The configuration property for the E-Mail subject template.
     */
    public static final String EMAIL_MESSAGE_SUBJECT_TEMPLATE_PROPERTY = "message.email.subject.template";
    /**
     * The configuration property for the E-Mail text body.
     */
    public static final String EMAIL_MESSAGE_TEXT_PROPERTY = "message.email.body_text.value";
    /**
     * The configuration property for the E-Mail text body template.
     */
    public static final String EMAIL_MESSAGE_TEXT_TEMPLATE_PROPERTY = "message.email.body_text.template";
    /**
     * The configuration property for the E-Mail html body.
     */
    public static final String EMAIL_MESSAGE_HTML_PROPERTY = "message.email.body_html.value";
    /**
     * The configuration property for the E-Mail html body template.
     */
    public static final String EMAIL_MESSAGE_HTML_TEMPLATE_PROPERTY = "message.email.body_html.template";
    /**
     * The configuration property for the E-Mail attachment templates.
     */
    public static final String EMAIL_MESSAGE_ATTACHMENT_TEMPLATES_PROPERTY = "message.email.attachment.templates";

    private final NotificationJobContext notificationJobContext;
    private final String from;
    private final String fromDisplayName;
    private final String replyTo;
    private final String replyToDisplayName;
    private final String envelopeFrom;
    private final Function<Locale, ResourceBundle> resourceBundleAccessor;
    private final TemplateProcessor<String> subjectTemplateProcessor;
    private final TemplateProcessor<String> textBodyTemplateProcessor;
    private final TemplateProcessor<String> htmlBodyTemplateProcessor;
    private final List<TemplateProcessor<Attachment>> attachmentProcessors;
    private final List<NotificationMessageResolverModelCustomizer> modelCustomizers;

    /**
     * Creates a new message resolver from the given notification job context and configuration source.
     * @param jobContext           The notification job context
     * @param configurationSource  The configuration source
     * @param modelCustomizers     A list of model customizers
     */
    public EmailNotificationMessageResolver(NotificationJobContext jobContext, ConfigurationSource configurationSource, List<NotificationMessageResolverModelCustomizer> modelCustomizers) {
        this.notificationJobContext = jobContext;
        this.from = configurationSource.getPropertyOrFail(EMAIL_MESSAGE_FROM_PROPERTY, String.class, Function.identity());
        this.fromDisplayName = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_FROM_NAME_PROPERTY, String.class, Function.identity(), o -> null);
        this.replyTo = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_REPLY_TO_PROPERTY, String.class, Function.identity(), o -> null);
        this.replyToDisplayName = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_REPLY_TO_NAME_PROPERTY, String.class, Function.identity(), o -> null);
        this.envelopeFrom = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_ENVELOP_FROM_PROPERTY, String.class, Function.identity(), o -> null);
        this.resourceBundleAccessor = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_RESOURCE_BUNDLE_PROPERTY, Function.class, EmailNotificationMessageResolver::resourceBundleByName, o -> null);
        TemplateContext templateContext = configurationSource.getPropertyOrDefault(EMAIL_TEMPLATE_CONTEXT_PROPERTY, TemplateContext.class, null, o -> jobContext.getService(TemplateContext.class));
        TemplateProcessorFactory<String> templateProcessorFactory = configurationSource.getPropertyOrDefault(
            EMAIL_TEMPLATE_PROCESSOR_TYPE_PROPERTY, TemplateProcessorFactory.class, s -> {
            if (templateContext == null) {
                throw new NotificationException("No template context given!");
            }
            return templateContext.getTemplateProcessorFactory(s, String.class);
        }, o -> null);
        Function<String, TemplateProcessorFactory<Attachment>> attachmentTemplateProcessorFactory = templateProcessorFactoryKey -> {
            if (templateContext == null) {
                throw new NotificationException("No template context given!");
            }
            return templateContext.getTemplateProcessorFactory(templateProcessorFactoryKey, Attachment.class);
        };
        Function<String, TemplateProcessor<String>> templateProcessorFunction = templateName ->
            templateProcessorByType(
                templateContext,
                templateProcessorFactory,
                configurationSource,
                templateName,
                jobContext);
        Function<String, TemplateProcessor<Attachment>> attachmentTemplateProcessorFunction = templateProcessorType -> templateProcessorByType(
            templateContext,
            attachmentTemplateProcessorFactory.apply(templateProcessorType),
            configurationSource,
            null,
            jobContext);
        String literalSubject = (String) configurationSource.getProperty(EMAIL_MESSAGE_SUBJECT_PROPERTY);
        this.subjectTemplateProcessor = literalSubject == null ? templateProcessorFunction.apply((String) configurationSource.getProperty(EMAIL_MESSAGE_SUBJECT_TEMPLATE_PROPERTY)) : TemplateProcessor.of(literalSubject);
        String literalBodyText = (String) configurationSource.getProperty(EMAIL_MESSAGE_TEXT_PROPERTY);
        this.textBodyTemplateProcessor = literalBodyText == null ? templateProcessorFunction.apply((String) configurationSource.getProperty(EMAIL_MESSAGE_TEXT_TEMPLATE_PROPERTY)) : TemplateProcessor.of(literalBodyText);
        String literalBodyHtml = (String) configurationSource.getProperty(EMAIL_MESSAGE_HTML_PROPERTY);
        this.htmlBodyTemplateProcessor = literalBodyHtml == null ? templateProcessorFunction.apply((String) configurationSource.getProperty(EMAIL_MESSAGE_HTML_TEMPLATE_PROPERTY)) : TemplateProcessor.of(literalBodyHtml);
        Object o = configurationSource.getProperty(EMAIL_MESSAGE_ATTACHMENT_TEMPLATES_PROPERTY);
        List<TemplateProcessor<Attachment>> attachmentProcessors = Collections.emptyList();
        if (o instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) o;
            attachmentProcessors = new ArrayList<>(collection.size());
            for (Object element : collection) {
                if (element instanceof String) {
                    attachmentProcessors.add(attachmentTemplateProcessorFunction.apply((String) element));
                } else {
                    throw new NotificationException("Invalid attachment processor given via property '" + EMAIL_MESSAGE_ATTACHMENT_TEMPLATES_PROPERTY
                        + "': " + element);
                }
            }
        } else if (o instanceof String) {
            attachmentProcessors = Collections.singletonList(attachmentTemplateProcessorFunction.apply((String) o));
        } else if (o != null) {
            throw new NotificationException("Invalid attachment processors given via property '" + EMAIL_MESSAGE_ATTACHMENT_TEMPLATES_PROPERTY
                + "': " + o);
        }
        this.attachmentProcessors = attachmentProcessors;
        this.modelCustomizers = modelCustomizers == null ? Collections.emptyList() : modelCustomizers;
    }

    /**
     * Creates a new message resolver.
     *
     * @param jobContext                The notification job context
     * @param from                      The from address
     * @param fromDisplayName           The from display name
     * @param replyTo                   The reply to address
     * @param replyToDisplayName        The reply to display name
     * @param envelopeFrom              The envelop from address
     * @param resourceBundleName        The resource bundle name
     * @param subjectTemplateProcessor  The subject template processor
     * @param textBodyTemplateProcessor The text body template processor
     * @param htmlBodyTemplateProcessor The html body template processor
     * @param attachmentProcessors      The attachment processors
     * @param modelCustomizers          A list of model customizers
     */
    public EmailNotificationMessageResolver(NotificationJobContext jobContext,
                                            String from, String fromDisplayName, String replyTo, String replyToDisplayName, String envelopeFrom, String resourceBundleName,
                                            TemplateProcessor<String> subjectTemplateProcessor, TemplateProcessor<String> textBodyTemplateProcessor,
                                            TemplateProcessor<String> htmlBodyTemplateProcessor, List<TemplateProcessor<Attachment>> attachmentProcessors,
                                            List<NotificationMessageResolverModelCustomizer> modelCustomizers) {
        this.notificationJobContext = jobContext;
        this.from = from;
        this.fromDisplayName = fromDisplayName;
        this.replyTo = replyTo;
        this.replyToDisplayName = replyToDisplayName;
        this.envelopeFrom = envelopeFrom;
        this.resourceBundleAccessor = resourceBundleByName(resourceBundleName);
        this.subjectTemplateProcessor = subjectTemplateProcessor;
        this.textBodyTemplateProcessor = textBodyTemplateProcessor;
        this.htmlBodyTemplateProcessor = htmlBodyTemplateProcessor;
        this.attachmentProcessors = attachmentProcessors == null ? Collections.emptyList() : attachmentProcessors;
        this.modelCustomizers = modelCustomizers == null ? Collections.emptyList() : modelCustomizers;
    }

    private static Function<Locale, ResourceBundle> resourceBundleByName(String name) {
        return locale -> ResourceBundle.getBundle(name, locale);
    }

    private static <T> TemplateProcessor<T> templateProcessorByType(TemplateContext templateContext, TemplateProcessorFactory<T> templateProcessorFactory, ConfigurationSource configurationSource, String templateName, ServiceProvider serviceProvider) {
        if (templateName == null) {
            return null;
        }
        if (templateContext == null) {
            throw new NotificationException("No template context given!");
        }
        if (templateProcessorFactory == null) {
            throw new NotificationException("No template processor factory given!");
        }
        return templateProcessorFactory.createTemplateProcessor(templateContext, templateName, configurationSource::getProperty, serviceProvider);
    }

    @Override
    public EmailNotificationMessage resolveNotificationMessage(Notification<?> notification) {
        Map<String, Object> model = new HashMap<>(notification.getJobConfiguration().getParameters());
        NotificationRecipient<?> notificationRecipient = notification.getRecipient();
        Locale locale = notificationRecipient.getLocale();
        if (resourceBundleAccessor != null) {
            ResourceBundle resourceBundle = resourceBundleAccessor.apply(locale);
            model.put("resourceBundle", resourceBundle);
        }
        model.put("locale", locale);
        model.put("timeZone", notificationRecipient.getTimeZone());
        model.put("recipient", notificationRecipient);
        for (NotificationMessageResolverModelCustomizer modelCustomizer : modelCustomizers) {
            modelCustomizer.customize(model, notification, notificationJobContext);
        }
        model = Collections.unmodifiableMap(model);

        String subjectString = subjectTemplateProcessor == null ? null : subjectTemplateProcessor.processTemplate(model);
        String textBodyString = textBodyTemplateProcessor == null ? null : textBodyTemplateProcessor.processTemplate(model);
        String htmlBodyString = htmlBodyTemplateProcessor == null ? null : htmlBodyTemplateProcessor.processTemplate(model);
        EmailSubject subject = subjectString == null ? null : new EmailSubject(subjectString);
        EmailBody textBody = textBodyString == null ? null : new EmailBody(textBodyString);
        EmailBody htmlBody = htmlBodyString == null ? null : new EmailBody(htmlBodyString);
        Collection<Attachment> attachments = new ArrayList<>(attachmentProcessors.size());
        for (TemplateProcessor attachmentTemplateProcessor : attachmentProcessors) {
            Object collectionOrAttachment = attachmentTemplateProcessor.processTemplate(model);
            if (collectionOrAttachment != null) {
                if (collectionOrAttachment instanceof Collection) {
                    for (Object attachment : (Collection) collectionOrAttachment) {
                        attachments.add((Attachment) attachment);
                    }
                } else {
                    attachments.add((Attachment) collectionOrAttachment);
                }
            }
        }
        return new EmailNotificationMessage(from, fromDisplayName, replyTo, replyToDisplayName, envelopeFrom, subject, textBody, htmlBody, attachments);
    }
}
