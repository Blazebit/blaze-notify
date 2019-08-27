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

import java.time.ZonedDateTime;
import java.util.List;

public class EmailBounceEvent extends EmailEvent {

	private static final long serialVersionUID = 1L;

	private final EmailEventBounceType bounceType;
	private final EmailEventBounceSubType bounceSubType;
	private final List<EmailEventBounceRecipient> bouncedRecipients;

	public EmailBounceEvent(EmailEventNotificationType notificationType, ZonedDateTime created, String messageId, EmailEventBounceType bounceType, EmailEventBounceSubType bounceSubType, List<EmailEventBounceRecipient> bouncedRecipients) {
		super(notificationType, created, messageId);
		this.bounceType = bounceType;
		this.bounceSubType = bounceSubType;
		this.bouncedRecipients = bouncedRecipients;
	}

	public EmailEventBounceType getBounceType() {
		return bounceType;
	}

	public EmailEventBounceSubType getBounceSubType() {
		return bounceSubType;
	}

	public List<EmailEventBounceRecipient> getBouncedRecipients() {
		return bouncedRecipients;
	}
}
