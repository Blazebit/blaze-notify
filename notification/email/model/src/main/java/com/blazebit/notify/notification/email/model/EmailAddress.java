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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class EmailAddress extends BaseEntity<String> {

	private static final long serialVersionUID = 1L;

	/**
	 * Whether the current email address is verified.
	 */
	private Boolean verified = false;
	/**
	 * Email addresses can be suppressed when emails to the address bounce or
	 * complaints are received.
	 */
	private Boolean suppressed = false;

	public EmailAddress() {
	}

	public EmailAddress(String email, Boolean verified, Boolean suppressed) {
		super(email == null ? null : email.toLowerCase());
		this.verified = verified;
		this.suppressed = suppressed;
	}

	public static EmailAddress verified(String address) {
		return new EmailAddress(address, true, false);
	}

	public static EmailAddress unverified(String address) {
		return new EmailAddress(address, false, false);
	}

	@Id
	@Email
	@Override
	@Column(name = "id", nullable = false, columnDefinition = ColumnTypes.MAIL_ADDRESS)
	public String getId() {
		return id();
	}

	@Override
	public void setId(String id) {
		if (id == null) {
			super.setId(null);
		} else {
			super.setId(id.toLowerCase());
		}
	}
	
	@NotNull
	@Column(nullable = false)
	public Boolean getVerified() {
		return verified;
	}

	public void setVerified(Boolean verified) {
		this.verified = verified;
	}

	@NotNull
	@Column(nullable = false)
	public Boolean getSuppressed() {
		return suppressed;
	}

	public void setSuppressed(Boolean suppressed) {
		this.suppressed = suppressed;
	}


}
