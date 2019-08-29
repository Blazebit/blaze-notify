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
package com.blazebit.notify.notification.email.model;

import com.blazebit.notify.job.ConfigurationSource;
import com.blazebit.notify.notification.ConfigurationSourceProvider;
import com.blazebit.notify.notification.NotificationJobContext;
import com.blazebit.notify.notification.NotificationRecipient;
import com.blazebit.notify.notification.email.message.Attachment;
import com.blazebit.notify.notification.email.message.EmailNotificationRecipient;
import com.blazebit.notify.notification.jpa.model.base.AbstractNotification;
import com.blazebit.notify.template.api.TemplateProcessor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

@MappedSuperclass
public abstract class AbstractEmailNotification<ID> extends AbstractNotification<ID> implements ConfigurationSourceProvider {

	private static final long serialVersionUID = 1L;

	public static final String TEMPLATE_PROCESSOR_TYPE_PARAMETER_NAME = "templateProcessorType";
	public static final String SUBJECT_PARAMETER_NAME = "subject";
	public static final String BODY_TEXT_PARAMETER_NAME = "text";
	public static final String BODY_HTML_PARAMETER_NAME = "html";
	public static final String ATTACHMENTS_PARAMETER_NAME = "attachments";
	private static final TemplateProcessor<String> SUBJECT_TEMPLATE_PROCESSOR = new ModelGetOrDelegateTemplateProcessor<>(SUBJECT_PARAMETER_NAME);
	private static final TemplateProcessor<String> BODY_TEXT_TEMPLATE_PROCESSOR = new ModelGetOrDelegateTemplateProcessor<>(BODY_TEXT_PARAMETER_NAME);
	private static final TemplateProcessor<String> BODY_HTML_TEMPLATE_PROCESSOR = new ModelGetOrDelegateTemplateProcessor<>(BODY_HTML_PARAMETER_NAME);
	private static final TemplateProcessor<Collection<Attachment>> ATTACHMENTS_TEMPLATE_PROCESSOR = new ModelGetOrDelegateTemplateProcessor<>(ATTACHMENTS_PARAMETER_NAME);

	private FromEmail from;
	private Long fromId;
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

	public AbstractEmailNotification() {
		super();
	}

	public AbstractEmailNotification(ID id) {
		super(id);
	}

	@Override
	public void markDone(Object result) {
		super.markDone(result);
		setMessageId((String) result);
	}

	@Override
	@Transient
	public NotificationRecipient getRecipient() {
		return EmailNotificationRecipient.of(to, Locale.getDefault(), to);
	}

	@Override
	public ConfigurationSource getConfigurationSource(NotificationJobContext context) {
		return new EmailNotificationConfigurationSource(this);
	}

	@Transient
	public String getTemplateProcessorType() {
		return (String) getJobConfiguration().getParameters().get(TEMPLATE_PROCESSOR_TYPE_PARAMETER_NAME);
	}

	public void setTemplateProcessorType(String templateProcessorType) {
		getJobConfiguration().getParameters().put(TEMPLATE_PROCESSOR_TYPE_PARAMETER_NAME, templateProcessorType);
	}

	@Transient
	public TemplateProcessor<String> getSubjectTemplateProcessor() {
		return SUBJECT_TEMPLATE_PROCESSOR;
	}

	public void setSubjectTemplateProcessor(TemplateProcessor<String> subjectTemplateProcessor) {
		getJobConfiguration().getParameters().put(SUBJECT_PARAMETER_NAME, (Serializable) subjectTemplateProcessor);
	}

	@Transient
	public TemplateProcessor<String> getBodyTextTemplateProcessor() {
		return BODY_TEXT_TEMPLATE_PROCESSOR;
	}

	public void setBodyTextTemplateProcessor(TemplateProcessor<String> bodyTextTemplateProcessor) {
		getJobConfiguration().getParameters().put(BODY_TEXT_PARAMETER_NAME, (Serializable) bodyTextTemplateProcessor);
	}

	@Transient
	public TemplateProcessor<String> getBodyHtmlTemplateProcessor() {
		return BODY_HTML_TEMPLATE_PROCESSOR;
	}

	public void setBodyHtmlTemplateProcessor(TemplateProcessor<String> bodyHtmlTemplateProcessor) {
		getJobConfiguration().getParameters().put(BODY_HTML_PARAMETER_NAME, (Serializable) bodyHtmlTemplateProcessor);
	}

	@Transient
	public TemplateProcessor<Collection<Attachment>> getAttachmentProcessor() {
		return ATTACHMENTS_TEMPLATE_PROCESSOR;
	}

	public void setAttachmentsTemplateProcessor(TemplateProcessor<Collection<Attachment>> attachmentsTemplateProcessor) {
		getJobConfiguration().getParameters().put(ATTACHMENTS_PARAMETER_NAME, (Serializable) attachmentsTemplateProcessor);
	}

	@Transient
	public String getSubject() {
		return (String) getJobConfiguration().getParameters().get(SUBJECT_PARAMETER_NAME);
	}

	public void setSubject(String subject) {
		getJobConfiguration().getParameters().put(SUBJECT_PARAMETER_NAME, subject);
	}

	@Transient
	public String getBodyText() {
		return (String) getJobConfiguration().getParameters().get(BODY_TEXT_PARAMETER_NAME);
	}

	public void setBodyText(String bodyText) {
		getJobConfiguration().getParameters().put(BODY_TEXT_PARAMETER_NAME, bodyText);
	}

	@Transient
	public String getBodyHtml() {
		return (String) getJobConfiguration().getParameters().get(BODY_HTML_PARAMETER_NAME);
	}

	public void setBodyHtml(String bodyHtml) {
		getJobConfiguration().getParameters().put(BODY_HTML_PARAMETER_NAME, bodyHtml);
	}

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "from_email", nullable = false)
	public FromEmail getFrom() {
		return from;
	}

	public void setFrom(FromEmail from) {
		this.from = from;
	}

	@Column(name = "from_email", nullable = false, insertable = false, updatable = false)
	public Long getFromId() {
		return fromId;
	}

	public void setFromId(Long fromId) {
		this.fromId = fromId;
	}

	@NotNull
	@Column(name = "to_email", nullable = false, columnDefinition = ColumnTypes.MAIL_RECIPIENT)
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@Column(name = "message_id", columnDefinition = ColumnTypes.MAIL_MESSAGE_ID)
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	@Column(name = "delivered_time")
	public Instant getDeliveredTime() {
		return deliveredTime;
	}

	public void setDeliveredTime(Instant deliveredTime) {
		this.deliveredTime = deliveredTime;
	}

	@Lob
	@Column(name = "delivery_notification")
	public String getDeliveryNotification() {
		return deliveryNotification;
	}

	public void setDeliveryNotification(String deliveryNotification) {
		this.deliveryNotification = deliveryNotification;
	}

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "delivery_state")
	public EmailNotificationDeliveryState getDeliveryState() {
		return deliveryState;
	}

	public void setDeliveryState(EmailNotificationDeliveryState deliveryState) {
		this.deliveryState = deliveryState;
	}

	@NotNull
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "review_state")
	public EmailNotificationReviewState getReviewState() {
		return reviewState;
	}

	public void setReviewState(EmailNotificationReviewState reviewState) {
		this.reviewState = reviewState;
	}

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
