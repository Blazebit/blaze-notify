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

import com.blazebit.notify.notification.email.sns.EmailBounceEvent;
import com.blazebit.notify.notification.email.sns.EmailComplaintEvent;
import com.blazebit.notify.notification.email.sns.EmailEvent;
import com.blazebit.notify.notification.email.sns.EmailEventBounceRecipient;

import javax.json.Json;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EmailEventHandler {

	public List<String> onEmailEvent(EmailEvent event, MailJob mailJob) {
		mailJob.setDelivered(Calendar.getInstance());
		
		List<String> suppressedEmails = new ArrayList<String>();
		
		switch (event.getNotificationType()) {
		case BOUNCE:
			EmailBounceEvent bounceEvent = (EmailBounceEvent) event;
			
			switch (bounceEvent.getBounceType()) {
			case TRANSIENT:
				// Manually review bounce
				mailJob.setReviewState(MailJobReviewState.NECESSARY);
				break;
			default:
				// Just set status and suppress emails
				for (EmailEventBounceRecipient recipient : bounceEvent.getBouncedRecipients()){
					suppressedEmails.add(recipient.getEmailAddress());
				}
				break;
			}
			
			mailJob.setDeliveryState(MailJobDeliveryState.BOUNCED);
			
			mailJob.setDeliveryNotification(Json.createObjectBuilder()
					.add("bounceType", bounceEvent.getBounceType().toString())
					.add("bounceSubType", bounceEvent.getBounceSubType().toString())
				.build()
			.toString());
			break;
		case COMPLAINT:
			EmailComplaintEvent complaintEvent = (EmailComplaintEvent) event;
			suppressedEmails.addAll(complaintEvent.getComplainedRecipients());
			mailJob.setDeliveryState(MailJobDeliveryState.COMPLAINED);
			
			mailJob.setDeliveryNotification(Json.createObjectBuilder()
					.add("complaintFeedbackType", complaintEvent.getComplaintFeedbackType().toString())
					.add("userAgent", complaintEvent.getUserAgent())
				.build()
			.toString());
			break;
		case DELIVERY:
			mailJob.setDeliveryState(MailJobDeliveryState.DELIVERED);
			break;
		}

		return suppressedEmails;
	}
	
}
