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

@Entity
public class SuppressedEmailReason extends EmbeddedIdEntity<SuppressedEmailReasonId> {

	private static final long serialVersionUID = 1L;

	private EmailAddress suppressedEmail;
	private MailJob mailJob;

	private Instant created;

	public SuppressedEmailReason() {
	}

	public SuppressedEmailReason(SuppressedEmailReasonId id) {
		super(id);
	}

	public SuppressedEmailReason(EmailAddress suppressedEmail, MailJob mailJob) {
		super(new SuppressedEmailReasonId(suppressedEmail.getId(), mailJob.getId()));
		this.suppressedEmail = suppressedEmail;
		this.mailJob = mailJob;
	}

	@EmbeddedId
	@Override
	public SuppressedEmailReasonId getId() {
		return id();
	}

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "email_address", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "suppressed_email_reason_fk_suppressed_email"))
	public EmailAddress getSuppressedEmail() {
		return suppressedEmail;
	}

	public void setSuppressedEmail(EmailAddress suppressedEmail) {
		this.suppressedEmail = suppressedEmail;
	}

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "mail_job_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "suppressed_email_reason_fk_mail_job"))
	public MailJob getMailJob() {
		return mailJob;
	}

	public void setMailJob(MailJob mailJob) {
		this.mailJob = mailJob;
	}

	@Column(nullable = false)
	public Instant getCreated() {
		return created;
	}

	public void setCreated(Instant created) {
		this.created = created;
	}

	@PrePersist
	public void onPersist() {
		if (this.created == null) {
			this.created = Instant.now();
		}
	}

}
