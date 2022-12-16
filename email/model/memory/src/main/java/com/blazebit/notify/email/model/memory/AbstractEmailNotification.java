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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * An abstract class implementing the {@link Notification} interface for E-Mail notifications.
 *
 * @param <ID> The id type of the notification
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractEmailNotification<ID> extends AbstractNotification<ID> implements ConfigurationSourceProvider {

    /**
     * The parameter name for the template processor type {@link com.blazebit.notify.template.api.TemplateProcessorKey#getTemplateProcessorType()}.
     */
    public static final String TEMPLATE_PROCESSOR_TYPE_PARAMETER = "templateProcessorType";
    /**
     * The parameter name for the subject which is a plain {@link String}.
     * The value represents the literal subject.
     */
    public static final String SUBJECT_PARAMETER = "subject";
    /**
     * The parameter name for the subject template which is a plain {@link String}.
     * The value refers to a template name.
     */
    public static final String SUBJECT_TEMPLATE_PARAMETER = "subjectTemplate";
    /**
     * The parameter name for the text body which is a plain {@link String}.
     * The value refers to the literal text body.
     */
    public static final String BODY_TEXT_PARAMETER = "text";
    /**
     * The parameter name for the text body template which is a plain {@link String}.
     * The value refers to a template name.
     */
    public static final String BODY_TEXT_TEMPLATE_PARAMETER = "textTemplate";
    /**
     * The parameter name for the html body which is a plain {@link String}.
     * The value refers to the literal html body.
     */
    public static final String BODY_HTML_PARAMETER = "html";
    /**
     * The parameter name for the html body template which is a plain {@link String}.
     * The value refers to a template name.
     */
    public static final String BODY_HTML_TEMPLATE_PARAMETER = "htmlTemplate";
    /**
     * The parameter name for the attachments which is a collection of plain {@link String}s referring to
     * template names. The templates are expected to be processed to a collection of
     * {@link Attachment} by a {@link TemplateProcessor}.
     */
    public static final String ATTACHMENT_TEMPLATES_PARAMETER = "attachmentTemplates";
    private static final long serialVersionUID = 1L;

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
        return EmailNotificationRecipient.of(to, Locale.getDefault(), TimeZone.getDefault(), to);
    }

    @Override
    public ConfigurationSource getConfigurationSource(NotificationJobContext context) {
        return new EmailNotificationConfigurationSource(this);
    }

    /**
     * Returns the template processor type to use for subject, text body. html body and attachments.
     *
     * @return the template processor type
     */
    public String getTemplateProcessorType() {
        return (String) getJobConfiguration().getParameters().get(TEMPLATE_PROCESSOR_TYPE_PARAMETER);
    }

    /**
     * Sets that subject, text body, html body and attachments should be processed by a template processor with the given type.
     *
     * @param templateProcessorType The template processor type
     */
    public void setTemplateProcessorType(String templateProcessorType) {
        getJobConfiguration().getParameters().put(TEMPLATE_PROCESSOR_TYPE_PARAMETER, templateProcessorType);
    }

    /**
     * Returns the subject template name.
     *
     * @return the subject template name
     */
    public String getSubjectTemplateName() {
        return (String) getJobConfiguration().getParameters().get(SUBJECT_TEMPLATE_PARAMETER);
    }

    /**
     * Sets the given subject template name.
     *
     * @param subjectTemplateName The subject template name
     */
    public void setSubjectTemplateName(String subjectTemplateName) {
        getJobConfiguration().getParameters().put(SUBJECT_TEMPLATE_PARAMETER, subjectTemplateName);
    }

    /**
     * Returns the subject string.
     *
     * @return the subject string
     */
    public String getSubject() {
        return (String) getJobConfiguration().getParameters().get(SUBJECT_PARAMETER);
    }

    /**
     * Sets the given subject.
     *
     * @param subject The subject
     */
    public void setSubject(String subject) {
        getJobConfiguration().getParameters().put(SUBJECT_PARAMETER, subject);
    }

    /**
     * Returns the text body template name.
     *
     * @return the text body template name
     */
    public String getBodyTextTemplateName() {
        return (String) getJobConfiguration().getParameters().get(BODY_TEXT_TEMPLATE_PARAMETER);
    }

    /**
     * Sets the given text body template name.
     *
     * @param bodyTextTemplateName The text body template name
     */
    public void setBodyTextTemplateName(String bodyTextTemplateName) {
        getJobConfiguration().getParameters().put(BODY_TEXT_TEMPLATE_PARAMETER, bodyTextTemplateName);
    }

    /**
     * Returns the body text string.
     *
     * @return the body text string
     */
    public String getBodyText() {
        return (String) getJobConfiguration().getParameters().get(BODY_TEXT_PARAMETER);
    }

    /**
     * Sets the given body text.
     *
     * @param bodyText The body text
     */
    public void setBodyText(String bodyText) {
        getJobConfiguration().getParameters().put(BODY_TEXT_PARAMETER, bodyText);
    }

    /**
     * Returns the html body template name.
     *
     * @return the html body template name
     */
    public String getBodyHtmlTemplateName() {
        return (String) getJobConfiguration().getParameters().get(BODY_HTML_TEMPLATE_PARAMETER);
    }

    /**
     * Sets the given html body template name.
     *
     * @param bodyHtmlTemplateName The html body template name
     */
    public void setBodyHtmlTemplateName(String bodyHtmlTemplateName) {
        getJobConfiguration().getParameters().put(BODY_HTML_TEMPLATE_PARAMETER, bodyHtmlTemplateName);
    }

    /**
     * Returns the body html string.
     *
     * @return the body html string
     */
    public String getBodyHtml() {
        return (String) getJobConfiguration().getParameters().get(BODY_HTML_PARAMETER);
    }

    /**
     * Sets the given body html.
     *
     * @param bodyHtml The body html
     */
    public void setBodyHtml(String bodyHtml) {
        getJobConfiguration().getParameters().put(BODY_HTML_PARAMETER, bodyHtml);
    }

    /**
     * Returns the attachment template names.
     *
     * @return the attachment template names
     */
    public List<Attachment> getAttachmentTemplates() {
        return (List<Attachment>) getJobConfiguration().getParameters().get(ATTACHMENT_TEMPLATES_PARAMETER);
    }

    /**
     * Sets the attachment template names.
     *
     * @param attachmentTemplates The attachment template names
     */
    public void setAttachmentTemplates(List<String> attachmentTemplates) {
        getJobConfiguration().getParameters().put(ATTACHMENT_TEMPLATES_PARAMETER, new ArrayList<>(attachmentTemplates));
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
}
