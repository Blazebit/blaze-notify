/*
 * Copyright 2018 - 2020 Blazebit.
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
import com.blazebit.notify.Notification;
import com.blazebit.notify.NotificationException;
import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.NotificationMessageResolver;
import com.blazebit.notify.NotificationRecipient;
import com.blazebit.notify.template.api.TemplateContext;
import com.blazebit.notify.template.api.TemplateProcessor;
import com.blazebit.notify.template.api.TemplateProcessorFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
    public static final String EMAIL_TEMPLATE_PROCESSOR_FACTORY_PROPERTY = "message.email.template_processor_factory";

    /**
     * The configuration property for the E-Mail subject.
     */
    public static final String EMAIL_MESSAGE_SUBJECT_PROPERTY = "message.email.subject";
    /**
     * The configuration property for the E-Mail text body.
     */
    public static final String EMAIL_MESSAGE_TEXT_PROPERTY = "message.email.text";
    /**
     * The configuration property for the E-Mail html body.
     */
    public static final String EMAIL_MESSAGE_HTML_PROPERTY = "message.email.html";
    /**
     * The configuration property for the E-Mail attachment processors.
     */
    public static final String EMAIL_MESSAGE_ATTACHMENT_PROCESSORS_PROPERTY = "message.email.attachment_processors";

    private final String from;
    private final String fromDisplayName;
    private final String replyTo;
    private final String replyToDisplayName;
    private final String envelopeFrom;
    private final Function<Locale, ResourceBundle> resourceBundleAccessor;
    private final TemplateProcessor<String> subjectTemplateProcessor;
    private final TemplateProcessor<String> textBodyTemplateProcessor;
    private final TemplateProcessor<String> htmlBodyTemplateProcessor;
    private final Collection<TemplateProcessor> attachmentProcessors;

    /**
     * Creates a new message resolver from the given notification job context and configuration source.
     *
     * @param jobContext          The notification job context
     * @param configurationSource The configuration source
     */
    public EmailNotificationMessageResolver(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
        this.from = configurationSource.getPropertyOrFail(EMAIL_MESSAGE_FROM_PROPERTY, String.class, Function.identity());
        this.fromDisplayName = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_FROM_NAME_PROPERTY, String.class, Function.identity(), o -> null);
        this.replyTo = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_REPLY_TO_PROPERTY, String.class, Function.identity(), o -> null);
        this.replyToDisplayName = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_REPLY_TO_NAME_PROPERTY, String.class, Function.identity(), o -> null);
        this.envelopeFrom = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_ENVELOP_FROM_PROPERTY, String.class, Function.identity(), o -> null);
        this.resourceBundleAccessor = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_RESOURCE_BUNDLE_PROPERTY, Function.class, s -> resourceBundleByName(s), o -> null);
        TemplateContext templateContext = configurationSource.getPropertyOrDefault(EMAIL_TEMPLATE_CONTEXT_PROPERTY, TemplateContext.class, null, o -> jobContext.getService(TemplateContext.class));
        TemplateProcessorFactory templateProcessorFactory = configurationSource.getPropertyOrDefault(EMAIL_TEMPLATE_PROCESSOR_FACTORY_PROPERTY, TemplateProcessorFactory.class, s -> {
            if (templateContext == null) {
                throw new NotificationException("No template context given!");
            }
            return templateContext.getTemplateProcessorFactory(s, String.class);
        }, o -> null);
        Function<String, TemplateProcessor> templateProcessorFunction = s -> templateProcessorByName(templateContext, templateProcessorFactory, configurationSource, s);
        this.subjectTemplateProcessor = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_SUBJECT_PROPERTY, TemplateProcessor.class, templateProcessorFunction, o -> null);
        this.textBodyTemplateProcessor = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_TEXT_PROPERTY, TemplateProcessor.class, templateProcessorFunction, o -> null);
        this.htmlBodyTemplateProcessor = configurationSource.getPropertyOrDefault(EMAIL_MESSAGE_HTML_PROPERTY, TemplateProcessor.class, templateProcessorFunction, o -> null);
        Object o = configurationSource.getProperty(EMAIL_MESSAGE_ATTACHMENT_PROCESSORS_PROPERTY);
        List<TemplateProcessor> attachmentProcessors = Collections.emptyList();
        if (o instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) o;
            attachmentProcessors = new ArrayList<>(collection.size());
            for (Object element : collection) {
                if (element instanceof TemplateProcessor<?>) {
                    attachmentProcessors.add((TemplateProcessor) element);
                } else if (element instanceof String) {
                    attachmentProcessors.add(templateProcessorFunction.apply((String) element));
                } else {
                    throw new NotificationException("Invalid attachment processor given via property '" + EMAIL_MESSAGE_ATTACHMENT_PROCESSORS_PROPERTY + "': " + element);
                }
            }
        } else if (o instanceof TemplateProcessor<?>) {
            attachmentProcessors = Arrays.asList((TemplateProcessor) o);
        } else if (o != null) {
            throw new NotificationException("Invalid attachment processors given via property '" + EMAIL_MESSAGE_ATTACHMENT_PROCESSORS_PROPERTY + "': " + o);
        }
        this.attachmentProcessors = attachmentProcessors;
    }

    /**
     * Creates a new message resolver.
     *
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
     */
    public EmailNotificationMessageResolver(String from, String fromDisplayName, String replyTo, String replyToDisplayName, String envelopeFrom, String resourceBundleName,
                                            TemplateProcessor<String> subjectTemplateProcessor, TemplateProcessor<String> textBodyTemplateProcessor,
                                            TemplateProcessor<String> htmlBodyTemplateProcessor, Collection<TemplateProcessor> attachmentProcessors) {
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
    }

    private static Function<Locale, ResourceBundle> resourceBundleByName(String name) {
        return locale -> ResourceBundle.getBundle(name, locale);
    }

    private static TemplateProcessor<String> templateProcessorByName(TemplateContext templateContext, TemplateProcessorFactory<String> templateProcessorFactory, ConfigurationSource configurationSource, String string) {
        if (templateContext == null) {
            throw new NotificationException("No template context given!");
        }
        if (templateProcessorFactory == null) {
            throw new NotificationException("No template processor factory given!");
        }
        return templateProcessorFactory.createTemplateProcessor(templateContext, key -> {
            if ("template".equals(key)) {
                return string;
            } else {
                return configurationSource.getProperty(key);
            }
        });
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
        model.put("recipient", notificationRecipient);
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
