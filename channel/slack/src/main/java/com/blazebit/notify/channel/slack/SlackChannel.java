/*
 * Copyright 2018 - 2020 Blazebit.
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
package com.blazebit.notify.channel.slack;

import com.blazebit.job.ConfigurationSource;
import com.blazebit.job.JobRateLimitException;
import com.blazebit.notify.Channel;
import com.blazebit.notify.ChannelKey;
import com.blazebit.notify.NotificationException;
import com.blazebit.notify.NotificationMessage;
import com.blazebit.notify.email.message.EmailNotificationRecipient;
import com.hubspot.algebra.Result;
import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.SlackClientFactory;
import com.hubspot.slack.client.SlackClientRuntimeConfig;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.methods.params.users.UserEmailParams;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.SlackErrorType;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * See https://api.slack.com/methods/chat.postMessage and https://api.slack.com/methods/users.lookupByEmail
 * <p>
 * See https://api.slack.com/docs/rate-limits for rate limiting
 * <p>
 * This channel needs a Slack App Bot User OAuth Access Token() with the permissions
 * <p>
 * * users:read
 * * users:read.email
 * <p>
 * This is required to resolve the user id by email.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SlackChannel implements Channel<EmailNotificationRecipient<?>, NotificationMessage> {

    /**
     * The key for which the channel is registered.
     */
    public static final ChannelKey<SlackChannel> KEY = ChannelKey.of("slack", SlackChannel.class);

    /**
     * The configuration property for the slack token.
     */
    public static final String SLACK_TOKEN_PROPERTY = "channel.slack.token";
    /**
     * The configuration property for the slack sender name.
     */
    public static final String SLACK_SENDER_NAME_PROPERTY = "channel.slack.sender_name";

    private static final Logger LOG = Logger.getLogger(SlackChannel.class.getName());

    private final SlackClient slackClient;
    private final String senderName;

    /**
     * Creates a new slack channel from the given configuration source.
     *
     * @param configurationSource The configuration source
     */
    public SlackChannel(ConfigurationSource configurationSource) {
        this(fromConfigurationSource(configurationSource));
    }

    /**
     * Creates a new slack channel from the given config.
     *
     * @param config The config
     */
    public SlackChannel(Config config) {
        if (config.token == null) {
            throw new IllegalArgumentException("No Slack token given for Slack channel!");
        }

        this.slackClient = SlackClientFactory.defaultFactory().build(
            SlackClientRuntimeConfig.builder()
                .setTokenSupplier(() -> config.token)
                .build()
        );
        this.senderName = config.senderName;
        LOG.log(Level.FINEST, "Slack channel opened");
    }

    private static Config fromConfigurationSource(ConfigurationSource configurationSource) {
        Config.Builder builder = Config.builder()
            .withToken(configurationSource.getPropertyOrFail(SLACK_TOKEN_PROPERTY, String.class, Function.identity()))
            .withSenderName(configurationSource.getPropertyOrFail(SLACK_SENDER_NAME_PROPERTY, String.class, Function.identity()));

        return builder.build();
    }

    @Override
    public void close() {
        try {
            this.slackClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<NotificationMessage> getNotificationMessageType() {
        return NotificationMessage.class;
    }

    @Override
    public Object sendNotificationMessage(EmailNotificationRecipient<?> recipient, NotificationMessage message) {
        try {
            String targetUserId = slackClient.lookupUserByEmail(UserEmailParams.builder().setEmail(recipient.getEmail()).build())
                .get()
                .unwrapOrElseThrow()
                .getUser()
                .getId();

            String text = message.toString();
            ChatPostMessageParams chatPostMessageParams = ChatPostMessageParams.builder()
                .setUsername(senderName)
                .setText(text)
                .setChannelId(targetUserId)
                .build();
            CompletableFuture<Result<ChatPostMessageResponse, SlackError>> resultCompletableFuture = slackClient.postMessage(chatPostMessageParams);
            Result<ChatPostMessageResponse, SlackError> result = resultCompletableFuture.get();
            ChatPostMessageResponse chatPostMessageResponse = result.unwrapOrElseThrow(error -> {
                if (error.getType() == SlackErrorType.RATE_LIMITED) {
                    return new JobRateLimitException(error.getError());
                } else {
                    return new NotificationException("Couldn't send Slack notification: " + error.getError());
                }
            });
            if (!chatPostMessageResponse.isOk()) {
                throw new NotificationException("Couldn't send Slack notification: " + chatPostMessageResponse.getMessage());
            }
            return null;
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Failed to send Slack notification", e);
            throw new NotificationException(e);
        }
    }

    /**
     * The configuration for the Slack channel.
     *
     * @author Christian Beikov
     * @since 1.0.0
     */
    public static class Config {
        private final String token;
        private final String senderName;

        /**
         * Create a new config.
         *
         * @param token      The slack token
         * @param senderName The sender name
         */
        Config(String token, String senderName) {
            this.token = token;
            this.senderName = senderName;
        }

        /**
         * Returns the slack token.
         *
         * @return the slack token
         */
        public String getToken() {
            return token;
        }

        /**
         * Returns the sender name.
         *
         * @return the sender name
         */
        public String getSenderName() {
            return senderName;
        }

        /**
         * Returns a new configuration builder.
         *
         * @return a new configuration builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * The configuration builder.
         *
         * @author Christian Beikov
         * @since 1.0.0
         */
        public static class Builder {
            private String token;
            private String senderName;

            /**
             * Returns the configuration.
             *
             * @return the configuration
             */
            public Config build() {
                return new Config(token, senderName);
            }

            /**
             * Sets the given slack token.
             *
             * @param token The slack token
             * @return <code>this</code> for chaining
             */
            public Builder withToken(String token) {
                this.token = token;
                return this;
            }

            /**
             * Sets the given sender name.
             *
             * @param senderName The sender name
             * @return <code>this</code> for chaining
             */
            public Builder withSenderName(String senderName) {
                this.senderName = senderName;
                return this;
            }
        }
    }
}
