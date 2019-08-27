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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Embeddable
public class SuppressedEmailReasonId implements Serializable {

	private static final long serialVersionUID = 1L;

	private String emailAddress;
	private Long mailJobId;

	public SuppressedEmailReasonId() {
		super();
	}

	public SuppressedEmailReasonId(String emailAddress, Long mailJobId) {
		if (emailAddress == null) {
			this.emailAddress = null;
		} else {
			this.emailAddress = emailAddress.toLowerCase();
		}
		this.mailJobId = mailJobId;
	}

	@NotNull
	@Column(name = "email_address", nullable = false, columnDefinition = ColumnTypes.MAIL_ADDRESS)
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		if (emailAddress == null) {
			this.emailAddress = null;
		} else {
			this.emailAddress = emailAddress.toLowerCase();
		}
	}

	@NotNull
	@Column(name = "mail_job_id", nullable = false)
	public Long getMailJobId() {
		return mailJobId;
	}

	public void setMailJobId(Long mailJobId) {
		this.mailJobId = mailJobId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((emailAddress == null) ? 0 : emailAddress.hashCode());
		result = prime * result
				+ ((mailJobId == null) ? 0 : mailJobId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SuppressedEmailReasonId other = (SuppressedEmailReasonId) obj;
		if (emailAddress == null) {
			if (other.emailAddress != null)
				return false;
		} else if (!emailAddress.equals(other.emailAddress))
			return false;
		if (mailJobId == null) {
			if (other.mailJobId != null)
				return false;
		} else if (!mailJobId.equals(other.mailJobId))
			return false;
		return true;
	}

}
