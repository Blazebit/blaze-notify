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

@Entity
public class FromEmail extends BaseEntity<Long> {
	
	private String email;
	private String name;
	private String replyToEmail;
	private String replyToName;

	public FromEmail() {
	}

	public FromEmail(Long id) {
		super(id);
	}

	public FromEmail(String email) {
		this.email = email;
	}

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
	public Long getId() {
		return id();
	}

	@NotNull
	@Column(name = "email", columnDefinition = ColumnTypes.MAIL_RECIPIENT, unique = true)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "reply_to_email", columnDefinition = ColumnTypes.MAIL_RECIPIENT)
	public String getReplyToEmail() {
		return replyToEmail;
	}

	public void setReplyToEmail(String replyToEmail) {
		this.replyToEmail = replyToEmail;
	}

	@Column(name = "reply_to_name")
	public String getReplyToName() {
		return replyToName;
	}

	public void setReplyToName(String replyToName) {
		this.replyToName = replyToName;
	}
}
