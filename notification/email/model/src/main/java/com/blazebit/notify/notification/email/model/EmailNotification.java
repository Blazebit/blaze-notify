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

import com.blazebit.notify.notification.ConfigurationSourceProvider;

import javax.persistence.*;

@Entity
@SequenceGenerator(name = "idGenerator", sequenceName = "email_notification_seq")
@Table(name = "email_notification")
public class EmailNotification extends AbstractEmailNotification<Long> implements ConfigurationSourceProvider {

	private static final long serialVersionUID = 1L;

	public EmailNotification() {
		super();
	}

	public EmailNotification(Long id) {
		super(id);
	}

	@Id
	@Override
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
	public Long getId() {
		return id();
	}

	@Override
	@Transient
	public Long getPartitionKey() {
		return id();
	}
}
