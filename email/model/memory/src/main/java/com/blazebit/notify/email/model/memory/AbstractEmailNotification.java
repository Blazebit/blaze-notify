/*
 * Copyright 2018 - 2022 Blazebit.
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
package com.blazebit.notify.email.model.memory;

import com.blazebit.job.ConfigurationSource;
import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.notify.ConfigurationSourceProvider;
import com.blazebit.notify.Notification;
import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.NotificationRecipient;
import com.blazebit.notify.email.message.Attachment;
import com.blazebit.notify.email.message.EmailNotificationRecipient;
import com.blazebit.notify.memory.model.AbstractNotification;
import com.blazebit.notify.template.api.TemplateProcessor;
import com.blazebit.notify.template.api.TemplateProcessorKey;

import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * An abstract class implementing the {@link Notification} interface for E-Mail notifications.
 *
 * @param <ID> The id type of the notification
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractEmailNotification<ID> extends AbstractNotification<ID> implements ConfigurationSourceProvider {

    /**
     * The parameter name for the template processor type {@link TemplateProcessorKey#getTemplateProcessorResultType()}.
     */
    public static final String TEMPLATE_PROCESSOR_TYPE_PARAMETER_NAME = "templateProcessorType";
    /**
     * The parameter name for the subject which can be a plain {@link String} or a {@link TemplateProcessor}.
     */
    public static final String SUBJECT_PARAMETER_NAME = "subject";
    /**
     * The parameter name for the text body which can be a plain {@link String} or a {@link TemplateProcessor}.
     */
    public static final String BODY_TEXT_PARAMETER_NAME = "text";
    /**
     * The parameter name for the html body which can be a plain {@link String} or a {@link TemplateProcessor}.
     */
    public static final String BODY_HTML_PARAMETER_NAME = "html";
    /**
     * The parameter name for the attachments represented by a {@link TemplateProcessor} producing a collection of {@link Attachment}.
     */
    public static final String ATTACHMENTS_PARAMETER_NAME = "attachments";

    private static final long serialVersionUID = 1L;

    private static final TemplateProcessor<String> SUBJECT_TEMPLATE_PROCESSOR = new ModelGetOrDelegateTemplateProcessor<>(SUBJECT_PARAMETER_NAME);
    private static final TemplateProcessor<String> BODY_TEXT_TEMPLATE_PROCESSOR = new ModelGetOrDelegateTemplateProcessor<>(BODY_TEXT_PARAMETER_NAME);
    private static final TemplateProcessor<String> BODY_HTML_TEMPLATE_PROCESSOR = new ModelGetOrDelegateTemplateProcessor<>(BODY_HTML_PARAMETER_NAME);
    private static final TemplateProcessor<Collection<Attachment>> ATTACHMENTS_TEMPLATE_PROCESSOR = new ModelGetOrDelegateTemplateProcessor<>(ATTACHMENTS_PARAMETER_NAME);

    private FromEmail from;
    private String to;

    // The message id of the message for tracking purposes
    private String messageId;
    // When the email was delivered
    private Instant deliveredTime;
    // A JSON string containing the notification information about the delivery
    private String deliveryNotification;
    // The state of the delivery i.e. delivered or bounced
    private EmailNotificationDeliveryState deliveryState;
    // The state of the mail job. Normally, mail jobs don't need to be reviewed, but when they bounce for no reason, they should be reviewed
    private EmailNotificationReviewState reviewState = EmailNotificationReviewState.UNNECESSARY;

    /**
     * Creates a notification with the given id.
     *
     * @param id The notification id
     */
    public AbstractEmailNotification(ID id) {
        super(id);
    }

    @Override
    public void markDone(JobInstanceProcessingContext<?> jobProcessingContext, Object result) {
        super.markDone(jobProcessingContext, result);
        setMessageId((String) result);
    }

    @Override
    public NotificationRecipient getRecipient() {
        return EmailNotificationRecipient.of(to, Locale.getDefault(), to);
    }

    @Override
    public ConfigurationSource getConfigurationSource(NotificationJobContext context) {
        return new EmailNotificationConfigurationSource(this);
    }

    /**
     * Returns the template processor type to use for subject, text body and html body.
     *
     * @return the template processor type
     */
    public String getTemplateProcessorType() {
        return (String) getJobConfiguration().getParameters().get(TEMPLATE_PROCESSOR_TYPE_PARAMETER_NAME);
    }

    /**
     * Sets that subject, text body and html body should be processed by a template processor with the given type.
     *
     * @param templateProcessorType The template processor type
     */
    public void setTemplateProcessorType(String templateProcessorType) {
        getJobConfiguration().getParameters().put(TEMPLATE_PROCESSOR_TYPE_PARAMETER_NAME, templateProcessorType);
    }

    /**
     * Returns the subject template processor.
     *
     * @return the subject template processor
     */
    public TemplateProcessor<String> getSubjectTemplateProcessor() {
        return SUBJECT_TEMPLATE_PROCESSOR;
    }

    /**
     * Sets the given subject template processor.
     *
     * @param subjectTemplateProcessor The template processor
     */
    public void setSubjectTemplateProcessor(TemplateProcessor<String> subjectTemplateProcessor) {
        getJobConfiguration().getParameters().put(SUBJECT_PARAMETER_NAME, subjectTemplateProcessor);
    }

    /**
     * Returns the text body template processor.
     *
     * @return the text body template processor
     */
    public TemplateProcessor<String> getBodyTextTemplateProcessor() {
        return BODY_TEXT_TEMPLATE_PROCESSOR;
    }

    /**
     * Sets the given text body template processor.
     *
     * @param bodyTextTemplateProcessor The template processor
     */
    public void setBodyTextTemplateProcessor(TemplateProcessor<String> bodyTextTemplateProcessor) {
        getJobConfiguration().getParameters().put(BODY_TEXT_PARAMETER_NAME, bodyTextTemplateProcessor);
    }

    /**
     * Returns the html body template processor.
     *
     * @return the html body template processor
     */
    public TemplateProcessor<String> getBodyHtmlTemplateProcessor() {
        return BODY_HTML_TEMPLATE_PROCESSOR;
    }

    /**
     * Sets the given html body template processor.
     *
     * @param bodyHtmlTemplateProcessor The template processor
     */
    public void setBodyHtmlTemplateProcessor(TemplateProcessor<String> bodyHtmlTemplateProcessor) {
        getJobConfiguration().getParameters().put(BODY_HTML_PARAMETER_NAME, bodyHtmlTemplateProcessor);
    }

    /**
     * Returns the attachment template processor.
     *
     * @return the attachment template processor
     */
    public TemplateProcessor<Collection<Attachment>> getAttachmentProcessor() {
        return ATTACHMENTS_TEMPLATE_PROCESSOR;
    }

    /**
     * Sets the given attachment template processor.
     *
     * @param attachmentsTemplateProcessor The template processor
     */
    public void setAttachmentsTemplateProcessor(TemplateProcessor<Collection<Attachment>> attachmentsTemplateProcessor) {
        getJobConfiguration().getParameters().put(ATTACHMENTS_PARAMETER_NAME, attachmentsTemplateProcessor);
    }

    /**
     * Returns the subject string.
     *
     * @return the subject string
     */
    public String getSubject() {
        return ((TemplateProcessor<String>) getJobConfiguration().getParameters().get(SUBJECT_PARAMETER_NAME)).processTemplate(null);
    }

    /**
     * Sets the given subject.
     *
     * @param subject The subject
     */
    public void setSubject(String subject) {
        getJobConfiguration().getParameters().put(SUBJECT_PARAMETER_NAME, TemplateProcessor.of(subject));
    }

    /**
     * Returns the text body string.
     *
     * @return the text body string
     */
    public String getBodyText() {
        return ((TemplateProcessor<String>) getJobConfiguration().getParameters().get(BODY_TEXT_PARAMETER_NAME)).processTemplate(null);
    }

    /**
     * Sets the given text body.
     *
     * @param bodyText The text body
     */
    public void setBodyText(String bodyText) {
        getJobConfiguration().getParameters().put(BODY_TEXT_PARAMETER_NAME, TemplateProcessor.of(bodyText));
    }

    /**
     * Returns the html body string.
     *
     * @return the html body string
     */
    public String getBodyHtml() {
        return ((TemplateProcessor<String>) getJobConfiguration().getParameters().get(BODY_HTML_PARAMETER_NAME)).processTemplate(null);
    }

    /**
     * Sets the given html body.
     *
     * @param bodyHtml The html body
     */
    public void setBodyHtml(String bodyHtml) {
        getJobConfiguration().getParameters().put(BODY_HTML_PARAMETER_NAME, TemplateProcessor.of(bodyHtml));
    }

    /**
     * Returns the subject template name.
     *
     * @return the subject template name
     */
    public String getSubjectTemplateName() {
        return (String) getJobConfiguration().getParameters().get(SUBJECT_PARAMETER_NAME);
    }

    /**
     * Sets the given subject template name.
     *
     * @param subjectTemplateName The subject template name
     */
    public void setSubjectTemplateName(String subjectTemplateName) {
        getJobConfiguration().getParameters().put(SUBJECT_PARAMETER_NAME, subjectTemplateName);
    }

    /**
     * Returns the text body template name.
     *
     * @return the text body template name
     */
    public String getBodyTextTemplateName() {
        return (String) getJobConfiguration().getParameters().get(BODY_TEXT_PARAMETER_NAME);
    }

    /**
     * Sets the given text body template name.
     *
     * @param bodyTextTemplateName The text body template name
     */
    public void setBodyTextTemplateName(String bodyTextTemplateName) {
        getJobConfiguration().getParameters().put(BODY_TEXT_PARAMETER_NAME, bodyTextTemplateName);
    }

    /**
     * Returns the html body template name.
     *
     * @return the html body template name
     */
    public String getBodyHtmlTemplateName() {
        return (String) getJobConfiguration().getParameters().get(BODY_HTML_PARAMETER_NAME);
    }

    /**
     * Sets the given html body template name.
     *
     * @param bodyHtmlTemplateName The html body template name
     */
    public void setBodyHtmlTemplateName(String bodyHtmlTemplateName) {
        getJobConfiguration().getParameters().put(BODY_HTML_PARAMETER_NAME, bodyHtmlTemplateName);
    }

    /**
     * Returns the {@link FromEmail}.
     *
     * @return the {@link FromEmail}
     */
    public FromEmail getFrom() {
        return from;
    }

    /**
     * Sets the {@link FromEmail}.
     *
     * @param from The {@link FromEmail}
     */
    public void setFrom(FromEmail from) {
        this.from = from;
    }

    /**
     * Returns the to E-Mail address.
     *
     * @return the to E-Mail address
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the to E-Mail address.
     *
     * @param to The to E-Mail address
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Returns the message id.
     *
     * @return the message id
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the message id.
     *
     * @param messageId The message id
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Returns the instant at which the E-Mail was delivered.
     *
     * @return the instant
     */
    public Instant getDeliveredTime() {
        return deliveredTime;
    }

    /**
     * Sets the instant when the E-Mail was delivered.
     *
     * @param deliveredTime The instant
     */
    public void setDeliveredTime(Instant deliveredTime) {
        this.deliveredTime = deliveredTime;
    }

    /**
     * Returns the delivery notification.
     *
     * @return the delivery notification
     */
    public String getDeliveryNotification() {
        return deliveryNotification;
    }

    /**
     * Sets the delivery notification.
     *
     * @param deliveryNotification The delivery notification
     */
    public void setDeliveryNotification(String deliveryNotification) {
        this.deliveryNotification = deliveryNotification;
    }

    /**
     * Returns the delivery state.
     *
     * @return the delivery state
     */
    public EmailNotificationDeliveryState getDeliveryState() {
        return deliveryState;
    }

    /**
     * Sets the delivery state.
     *
     * @param deliveryState The delivery state
     */
    public void setDeliveryState(EmailNotificationDeliveryState deliveryState) {
        this.deliveryState = deliveryState;
    }

    /**
     * Returns the review state.
     *
     * @return the review state
     */
    public EmailNotificationReviewState getReviewState() {
        return reviewState;
    }

    /**
     * Sets the review state.
     *
     * @param reviewState The review state
     */
    public void setReviewState(EmailNotificationReviewState reviewState) {
        this.reviewState = reviewState;
    }

    /**
     * A template processor delegating to a parameter of the model map.
     *
     * @author Christian Beikov
     * @since 1.0.0
     */
    private static class ModelGetOrDelegateTemplateProcessor<T> implements TemplateProcessor<T> {

        private final String parameter;

        public ModelGetOrDelegateTemplateProcessor(String parameter) {
            this.parameter = parameter;
        }

        @Override
        public T processTemplate(Map<String, Object> model) {
            Object o = model.get(parameter);
            if (o instanceof TemplateProcessor<?>) {
                return ((TemplateProcessor<T>) o).processTemplate(model);
            } else {
                return (T) o;
            }
        }
    }
}
