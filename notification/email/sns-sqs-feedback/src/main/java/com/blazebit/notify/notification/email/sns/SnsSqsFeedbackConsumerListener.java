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

import java.io.StringReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.json.*;

import com.blazebit.notify.actor.spi.ConsumerListener;

public abstract class SnsSqsFeedbackConsumerListener implements ConsumerListener<Message> {
	
	private static final Logger LOG = Logger.getLogger(SnsSqsFeedbackConsumerListener.class.getName());
	private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ISO_ZONED_DATE_TIME;
	private static final int MAXIMUM_RETRY_COUNT = 5;

	@Override
	public void consume(List<Message> messages) {
		List<EmailEvent> list = new ArrayList<>(messages.size());
		for (int i = 0; i < messages.size(); i++) {
			Message message = messages.get(i);
			int deliveryCount = 0;

			try {
				deliveryCount = message.getIntProperty("JMSXDeliveryCount");
				list.add(createEvent(message));
			} catch (JMSException e) {
				LOG.log(Level.SEVERE, "Could not process email event message!", e);
			} catch (Throwable e) {
				if (deliveryCount >= MAXIMUM_RETRY_COUNT) {
					LOG.log(Level.SEVERE, "Dropping email event message because it reached maximum retry count of " + MAXIMUM_RETRY_COUNT + "!", e);
				} else {
					throw new IllegalArgumentException("Could not process email event message on the " + deliveryCount + "th try!", e);
				}
			}
		}

		handleEvents(list);
	}

	protected abstract void handleEvents(List<EmailEvent> events);

	protected EmailEvent createEvent(Message message) throws JMSException {
		String json = ((TextMessage) message).getText();
		JsonReader reader = Json.createReader(new StringReader(json));
		JsonObject eventObject = reader.readObject();

		EmailEventNotificationType notificationType = EmailEventNotificationType.fromString(eventObject.getString("notificationType"));

		JsonObject mailObject = eventObject.getJsonObject("mail");

		if (mailObject == null) {
			throw new IllegalArgumentException("Mail object missing!");
		}

		String messageId = mailObject.getString("messageId");
		ZonedDateTime created;
		JsonArray jsonArray;

		// See https://docs.aws.amazon.com/ses/latest/DeveloperGuide/notification-contents.html
		switch (notificationType) {
			case BOUNCE:
				JsonObject bounceObject = eventObject.getJsonObject("bounce");

				if (bounceObject == null) {
					throw new IllegalArgumentException("Bounce type should have a bounce object!");
				}

				created = ZonedDateTime.from(ISO8601.parse(bounceObject.getString("timestamp")));

				EmailEventBounceType bounceType = EmailEventBounceType.fromString(bounceObject.getString("bounceType"));
				EmailEventBounceSubType bounceSubType = EmailEventBounceSubType.fromString(bounceObject.getString("bounceSubType"));
				jsonArray = bounceObject.getJsonArray("bouncedRecipients");

				List<EmailEventBounceRecipient> bouncedRecipients = new ArrayList<>(jsonArray.size());
				for (JsonValue arrayValue : jsonArray) {
					JsonObject recipientObject = (JsonObject) arrayValue;
					// NOTE: action, status and diagnosticCode are optional
					// TODO: extract diagnostics code etc.
					bouncedRecipients.add(new EmailEventBounceRecipient(recipientObject.getString("emailAddress")));
				}

				return new EmailBounceEvent(
						notificationType,
						created,
						messageId,
						bounceType,
						bounceSubType,
						bouncedRecipients
				);
			case COMPLAINT:
				JsonObject complaintObject = eventObject.getJsonObject("complaint");

				if (complaintObject == null) {
					throw new IllegalArgumentException("Complaint type should have a complaint object!");
				}

				created = ZonedDateTime.from(ISO8601.parse(complaintObject.getString("timestamp")));

				// NOTE: these are optional, therefore we use a default
				EmailEventComplaintFeedbackType complaintFeedbackType = EmailEventComplaintFeedbackType.fromString(complaintObject.getString("complaintFeedbackType", "other"));
				String userAgent = complaintObject.getString("userAgent", null);
				jsonArray = complaintObject.getJsonArray("complainedRecipients");

				List<String> complainedRecipients = new ArrayList<>(jsonArray.size());
				for (JsonValue arrayValue : jsonArray) {
					JsonObject recipientObject = (JsonObject) arrayValue;
					complainedRecipients.add(recipientObject.getString("emailAddress"));
				}

				return new EmailComplaintEvent(
						notificationType,
						created,
						messageId,
						complaintFeedbackType,
						complainedRecipients,
						userAgent
				);
			case DELIVERY:
				JsonObject deliveryObject = eventObject.getJsonObject("delivery");

				if (deliveryObject == null) {
					throw new IllegalArgumentException("Delivery type should have a delivery object!");
				}

				created = ZonedDateTime.from(ISO8601.parse(deliveryObject.getString("timestamp")));
				jsonArray = deliveryObject.getJsonArray("recipients");

				List<String> recipients = new ArrayList<>(jsonArray.size());
				for (JsonValue arrayValue : jsonArray) {
					JsonString stringValue = (JsonString) arrayValue;
					recipients.add(stringValue.getString());
				}

				return new EmailDeliveryEvent(
						notificationType,
						created,
						messageId,
						recipients
				);
			default:
				throw new IllegalArgumentException("Unknown notification type: " + notificationType);
		}
	}

}
