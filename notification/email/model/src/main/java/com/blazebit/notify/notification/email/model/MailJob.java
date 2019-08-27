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

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Calendar;

@Entity
@SequenceGenerator(name = "idGenerator", sequenceName = "mail_job_seq")
@Table(name = "mail_job")
// TODO: make compatible with blaze-notify-job
public class MailJob extends BaseEntity<Long> {

	private static final long serialVersionUID = 1L;

	private String subject;
	private String bodyText;
	private String bodyHtml;
	
	private FromEmail from;
	private String to;

	private Instant created;
	private Instant scheduledTime;

	// The time at which the email was sent
	private Instant sent;
	// The message id of the message for tracking purposes
	private String messageId;
	// The send status of the email
	private MailJobState sendState;

	// When the email was delivered
	private Calendar delivered;
	// A JSON string containing the notification information about the delivery
	private String deliveryNotification;
	private MailJobDeliveryState deliveryState;
	// The state of the mail job. Normally, mail jobs don't need to be reviewed, but when they bounce for no reason, they should be reviewed
	private MailJobReviewState reviewState = MailJobReviewState.UNNECESSARY;

	public MailJob() {
		super();
	}

	public MailJob(Long id) {
		super(id);
	}

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
	public Long getId() {
		return id();
	}

	@Column(name = "mail_subject", columnDefinition = ColumnTypes.MAIL_SUBJECT)
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@NotNull
	@Column(nullable = false, columnDefinition = ColumnTypes.MAIL_CONTENT)
	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	@Column(columnDefinition = ColumnTypes.MAIL_CONTENT)
	public String getBodyHtml() {
		return bodyHtml;
	}

	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
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

	@NotNull
	@Column(name = "to_email", nullable = false, columnDefinition = ColumnTypes.MAIL_RECIPIENT)
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@Column(nullable = false)
	public Instant getCreated() {
		return created;
	}

	public void setCreated(Instant created) {
		this.created = created;
	}

	@Column(nullable = false)
	public Instant getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(Instant scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public Instant getSent() {
		return sent;
	}

	public void setSent(Instant sent) {
		this.sent = sent;
	}

	@Column(columnDefinition = ColumnTypes.MAIL_MESSAGE_ID)
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	@NotNull
	@Enumerated(EnumType.ORDINAL)
	public MailJobState getSendState() {
		return sendState;
	}

	public void setSendState(MailJobState sendState) {
		this.sendState = sendState;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getDelivered() {
		return delivered;
	}

	public void setDelivered(Calendar delivered) {
		this.delivered = delivered;
	}

	@Column(columnDefinition = ColumnTypes.MAIL_NOTIFICATION)
	public String getDeliveryNotification() {
		return deliveryNotification;
	}

	public void setDeliveryNotification(String deliveryNotification) {
		this.deliveryNotification = deliveryNotification;
	}

	@Enumerated(EnumType.ORDINAL)
	public MailJobDeliveryState getDeliveryState() {
		return deliveryState;
	}

	public void setDeliveryState(MailJobDeliveryState deliveryState) {
		this.deliveryState = deliveryState;
	}

	@NotNull
	@Enumerated(EnumType.ORDINAL)
	public MailJobReviewState getReviewState() {
		return reviewState;
	}

	public void setReviewState(MailJobReviewState reviewState) {
		this.reviewState = reviewState;
	}

	@PrePersist
	public void onPersist() {
		if (this.created == null) {
			this.created = Instant.now();
		}
	}
}
