/*
 * Copyright 2018 - 2025 Blazebit.
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
package com.blazebit.notify.channel.ses;

import com.blazebit.job.ConfigurationSource;
import com.blazebit.job.JobRateLimitException;
import com.blazebit.notify.Channel;
import com.blazebit.notify.ChannelKey;
import com.blazebit.notify.NotificationException;
import com.blazebit.notify.email.message.Attachment;
import com.blazebit.notify.email.message.EmailBody;
import com.blazebit.notify.email.message.EmailNotificationMessage;
import com.blazebit.notify.email.message.EmailNotificationRecipient;
import com.sun.mail.smtp.SMTPMessage;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A channel that sends messages via AWS SES.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SesChannel implements Channel<EmailNotificationRecipient<?>, EmailNotificationMessage> {

    /**
     * The key for which the channel is registered.
     */
    public static final ChannelKey<SesChannel> KEY = ChannelKey.of("ses", SesChannel.class);

    /**
     * The configuration property for the AWS SES access key.
     */
    public static final String SES_ACCESS_KEY_PROPERTY = "channel.ses.access_key";
    /**
     * The configuration property for the AWS SES secret key.
     */
    public static final String SES_SECRET_KEY_PROPERTY = "channel.ses.secret_key";
    /**
     * The configuration property for the AWS SES region.
     */
    public static final String SES_REGION_PROPERTY = "channel.ses.region";

    private static final Logger LOG = Logger.getLogger(SesChannel.class.getName());

    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String HTML_MIME_TYPE = "text/html; charset=" + CHARSET_UTF8;

    private final Session session = Session.getDefaultInstance(new Properties());
    private final SesClient client;

    /**
     * Creates a new SES channel from the given configuration source.
     *
     * @param configurationSource The configuration source
     */
    public SesChannel(ConfigurationSource configurationSource) {
        this(fromConfigurationSource(configurationSource));
    }

    /**
     * Creates a new SES channel from the given config.
     *
     * @param config The config
     */
    public SesChannel(Config config) {
        AwsCredentialsProvider credentials;

        if (config.accessKey != null && config.secretKey != null) {
            credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(config.accessKey, config.secretKey));
        } else {
            throw new IllegalArgumentException("No AWS access key and secret key given for SES channel!");
        }

        Region region;
        if (config.region == null) {
            throw new IllegalArgumentException("No AWS region given for SES channel!");
        } else {
            region = Region.of(config.region);
        }

        client = SesClient.builder()
            .credentialsProvider(credentials)
            .region(region)
            .build();
        LOG.log(Level.FINEST, "SES transport opened");
    }

    private static Config fromConfigurationSource(ConfigurationSource configurationSource) {
        Config.Builder builder = Config.builder()
            .withAccessKey(configurationSource.getPropertyOrFail(SES_ACCESS_KEY_PROPERTY, String.class, Function.identity()))
            .withSecretKey(configurationSource.getPropertyOrFail(SES_SECRET_KEY_PROPERTY, String.class, Function.identity()))
            .withRegion(configurationSource.getPropertyOrFail(SES_REGION_PROPERTY, String.class, Function.identity()));

        return builder.build();
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public Class<EmailNotificationMessage> getNotificationMessageType() {
        return EmailNotificationMessage.class;
    }

    @Override
    public Object sendNotificationMessage(EmailNotificationRecipient<?> recipient, EmailNotificationMessage message) {
        try {
            SMTPMessage msg = new SMTPMessage(session);

            EmailBody textBody = message.getTextBody();
            EmailBody htmlBody = message.getHtmlBody();
            boolean hasAttachments = !message.getAttachments().isEmpty();
            if (textBody != null && htmlBody != null || hasAttachments) {
                Multipart multipart = new MimeMultipart("alternative");
                if (textBody != null) {
                    MimeBodyPart textPart = new MimeBodyPart();
                    textPart.setText(textBody.getBody(), CHARSET_UTF8);
                    multipart.addBodyPart(textPart);
                }

                if (htmlBody != null) {
                    MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setContent(htmlBody.getBody(), HTML_MIME_TYPE);
                    multipart.addBodyPart(htmlPart);
                }

                for (Attachment attachment : message.getAttachments()) {
                    multipart.addBodyPart(createAttachmentBodyPart(attachment.getName(), attachment.getDataSource()));
                }

                msg.setContent(multipart);
            } else if (textBody != null) {
                msg.setText(textBody.getBody(), CHARSET_UTF8);
            } else if (htmlBody != null) {
                msg.setContent(htmlBody.getBody(), HTML_MIME_TYPE);
            }

            String from = message.getFrom();
            String fromDisplayName = message.getFromDisplayName();
            msg.setFrom(toInternetAddress(from, fromDisplayName));

            String replyTo = message.getReplyTo();
            if (replyTo != null && !replyTo.isEmpty()) {
                msg.setReplyTo(new Address[]{toInternetAddress(replyTo, message.getReplyToDisplayName())});
            }
            String envelopeFrom = message.getEnvelopeFrom();
            if (envelopeFrom != null && !envelopeFrom.isEmpty()) {
                msg.setEnvelopeFrom(envelopeFrom);
            }

            msg.setHeader("To", recipient.getEmail());
            msg.setSubject(message.getSubject().getSubject(), CHARSET_UTF8);

            // Build the raw message
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            msg.writeTo(outputStream);
            RawMessage rawMessage = RawMessage.builder()
                .data(SdkBytes.fromByteArray(outputStream.toByteArray()))
                .build();

            // Send the email.
            SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
                .rawMessage(rawMessage)
                .build();
            SendRawEmailResponse result = client.sendRawEmail(rawEmailRequest);

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "SES notification sent to " + recipient + " with message id: " + result.messageId());
            }
            return result.messageId();
        } catch (SesException e) {
            if ("Throttling".equals(e.awsErrorDetails().errorCode())) {
                LOG.log(Level.FINE, "Rate limit exceeded", e);
                // TODO: parse if daily or second rate limit and adapt wait time
                throw new JobRateLimitException(e);
            }
            LOG.log(Level.SEVERE, "Failed to send email", e);
            throw new NotificationException(e);
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Failed to send email", e);
            throw new NotificationException(e);
        }
    }

    private MimeBodyPart createAttachmentBodyPart(String attachmentFilename, DataSource dataSource) throws MessagingException {
        try {
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setDisposition(MimeBodyPart.ATTACHMENT);
            mimeBodyPart.setFileName(MimeUtility.encodeText(attachmentFilename));
            mimeBodyPart.setDataHandler(new DataHandler(dataSource));
            return mimeBodyPart;
        } catch (UnsupportedEncodingException ex) {
            throw new MessagingException("Failed to encode attachment filename", ex);
        }
    }

    private InternetAddress toInternetAddress(String email, String displayName) throws UnsupportedEncodingException, AddressException {
        if (email == null || "".equals(email.trim())) {
            throw new IllegalArgumentException("Please provide a valid address", null);
        }
        if (displayName == null || "".equals(displayName.trim())) {
            return new InternetAddress(email);
        }
        return new InternetAddress(email, displayName, "utf-8");
    }

    /**
     * The configuration for the SES channel.
     *
     * @author Christian Beikov
     * @since 1.0.0
     */
    public static class Config {
        private final String accessKey;
        private final String secretKey;
        private final String region;

        /**
         * Create a new config.
         *
         * @param accessKey The AWS SES access key
         * @param secretKey The AWS SES secret key
         * @param region    The AWS SES region
         */
        Config(String accessKey, String secretKey, String region) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.region = region;
        }

        /**
         * Returns the AWS SES access key.
         *
         * @return the AWS SES access key
         */
        public String getAccessKey() {
            return accessKey;
        }

        /**
         * Returns the AWS SES secret key.
         *
         * @return the AWS SES secret key
         */
        public String getSecretKey() {
            return secretKey;
        }

        /**
         * Returns the AWS SES region.
         *
         * @return the AWS SES region
         */
        public String getRegion() {
            return region;
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
            private String accessKey;
            private String secretKey;
            private String region;

            /**
             * Returns the configuration.
             *
             * @return the configuration
             */
            public Config build() {
                return new Config(accessKey, secretKey, region);
            }

            /**
             * Sets the AWS SES access key.
             *
             * @param accessKey The AWS SES access key
             * @return <code>this</code> for chaining
             */
            public Builder withAccessKey(String accessKey) {
                this.accessKey = accessKey;
                return this;
            }

            /**
             * Sets the AWS SES secret key.
             *
             * @param secretKey The AWS SES secret key
             * @return <code>this</code> for chaining
             */
            public Builder withSecretKey(String secretKey) {
                this.secretKey = secretKey;
                return this;
            }

            /**
             * Sets the AWS SES region.
             *
             * @param region The AWS SES region
             * @return <code>this</code> for chaining
             */
            public Builder withRegion(String region) {
                this.region = region;
                return this;
            }
        }
    }
}
