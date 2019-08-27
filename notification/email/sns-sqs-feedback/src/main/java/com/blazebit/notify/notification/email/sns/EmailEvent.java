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
package com.blazebit.notify.notification.email.sns;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class EmailEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private final EmailEventNotificationType notificationType;
	private final ZonedDateTime created;
	private final String messageId;

	public EmailEvent(EmailEventNotificationType notificationType, ZonedDateTime created, String messageId) {
		this.notificationType = notificationType;
		this.created = created;
		this.messageId = messageId;
	}

	public EmailEventNotificationType getNotificationType() {
		return notificationType;
	}

	public ZonedDateTime getCreated() {
		return created;
	}

	public String getMessageId() {
		return messageId;
	}

}
