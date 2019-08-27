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
package com.blazebit.notify.notification.channel.smtp;

import com.blazebit.exception.ExceptionUtils;
import com.blazebit.notify.job.ConfigurationSource;
import com.blazebit.notify.job.JobRateLimitException;
import com.blazebit.notify.job.JobTemporaryException;
import com.blazebit.notify.notification.*;
import com.blazebit.notify.notification.email.message.Attachment;
import com.blazebit.notify.notification.email.message.EmailBody;
import com.blazebit.notify.notification.email.message.EmailNotificationMessage;
import com.blazebit.notify.notification.email.message.EmailNotificationRecipient;
import com.blazebit.notify.notification.security.HostnameVerificationPolicy;
import com.blazebit.notify.notification.security.JSSETruststoreConfigurator;
import com.blazebit.notify.notification.security.TruststoreProvider;
import com.blazebit.notify.notification.security.TruststoreProviderFactory;
import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPTransport;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.net.ssl.SSLSocketFactory;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmtpChannel implements Channel<EmailNotificationRecipient<?>, EmailNotificationMessage> {

    public static final ChannelKey<SmtpChannel> KEY = ChannelKey.of("smtp", SmtpChannel.class);

    public static final String SMTP_HOST_PROPERTY = "channel.smtp.host";
    public static final String SMTP_PORT_PROPERTY = "channel.smtp.port";
    public static final String SMTP_USER_PROPERTY = "channel.smtp.user";
    public static final String SMTP_PASSWORD_PROPERTY = "channel.smtp.password";
    public static final String SMTP_CONNECTION_TIMEOUT_PROPERTY = "channel.smtp.connection_timeout";
    public static final String SMTP_TIMEOUT_PROPERTY = "channel.smtp.timout";
    public static final String SMTP_ENABLE_SSL_PROPERTY = "channel.smtp.enable_ssl";
    public static final String SMTP_ENABLE_STARTTLS_PROPERTY = "channel.smtp.enable_starttls";
    public static final String SMTP_NODE_NAME_PROPERTY = "channel.smtp.node_name";
    public static final String SMTP_FILTER_PROPERTY = "channel.smtp.filter";

    private static final Logger LOG = Logger.getLogger(SmtpChannel.class.getName());

    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String HTML_MIME_TYPE = "text/html; charset=" + CHARSET_UTF8;

    private final Config config;
    private final Session session;
    private final BlockingQueue<Transport> transports;

    public SmtpChannel(ConfigurationSource configurationSource) {
        this(fromConfigurationSource(configurationSource));
    }

    private static Config fromConfigurationSource(ConfigurationSource configurationSource) {
        Config.Builder builder = Config.builder()
                .withHost(configurationSource.getPropertyOrFail(SMTP_HOST_PROPERTY, String.class, Function.identity()))
                .withPort(configurationSource.getPropertyOrFail(SMTP_PORT_PROPERTY, Integer.class, Integer::valueOf))
                .withConnectionTimeout(configurationSource.getPropertyOrDefault(SMTP_CONNECTION_TIMEOUT_PROPERTY, Long.class, Long::valueOf, o -> 10000L))
                .withTimeout(configurationSource.getPropertyOrDefault(SMTP_TIMEOUT_PROPERTY, Long.class, Long::valueOf, o -> 10000L))
                .withEnableSsl(configurationSource.getPropertyOrDefault(SMTP_ENABLE_SSL_PROPERTY, Boolean.class, Boolean::valueOf, o -> false))
                .withEnableStartTls(configurationSource.getPropertyOrDefault(SMTP_ENABLE_STARTTLS_PROPERTY, Boolean.class, Boolean::valueOf, o -> false))
                .withNodeName(configurationSource.getPropertyOrDefault(SMTP_NODE_NAME_PROPERTY, String.class, Function.identity(), o -> null))
                .withFilter(configurationSource.getPropertyOrDefault(SMTP_FILTER_PROPERTY, SmtpChannelFilter.class, null, o -> null));

        String user = configurationSource.getPropertyOrDefault(SMTP_USER_PROPERTY, String.class, Function.identity(), o -> null);
        String password = configurationSource.getPropertyOrDefault(SMTP_PASSWORD_PROPERTY, String.class, Function.identity(), o -> null);

        if (user != null && !user.isEmpty() || password != null && !password.isEmpty()) {
            builder.withAuth(user, password);
        }

        // TODO: trust store config?

        return builder.build();
    }

    public SmtpChannel(Config config) {
        this.config = config;

        Properties props = new Properties();

        if (config.host != null) {
            props.setProperty("mail.smtp.host", config.host);
        }

        if (config.port != null) {
            props.setProperty("mail.smtp.port", config.port.toString());
        }

        if (config.auth) {
            props.setProperty("mail.smtp.auth", "true");
        }

        if (config.enableSsl) {
            props.setProperty("mail.smtp.ssl.enable", "true");
        }

        if (config.enableStartTls) {
            props.setProperty("mail.smtp.starttls.enable", "true");
        }

        if (config.enableSsl || config.enableStartTls) {
            setupTruststore(props);
        }

        props.setProperty("mail.smtp.timeout", Long.toString(config.timeout));
        props.setProperty("mail.smtp.connectiontimeout", Long.toString(config.connectionTimeout));

        if (config.nodeName != null) {
            props.setProperty("mail.from", config.nodeName);
        }

        session = Session.getInstance(props);
        transports = new ArrayBlockingQueue<>(config.connectionPoolSize);
        for (int i = 0; i < config.connectionPoolSize; i++) {
            try {
                transports.add(session.getTransport("smtp"));
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
        }
        LOG.log(Level.FINEST, "SMTP transport opened");
    }

    @Override
    public void close() {
        Throwable firstThrowable = null;
        for (Transport transport : transports) {
            try {
                transport.close();
            } catch (MessagingException e) {
                LOG.log(Level.WARNING, "Failed to close transport", e);
            } catch (Throwable t) {
                firstThrowable = firstThrowable == null ? t : firstThrowable;
            }
        }
        LOG.log(Level.FINEST, "SMTP transport closed");
        if (firstThrowable != null) {
            ExceptionUtils.doThrow(firstThrowable);
        }
    }

    @Override
    public Class<EmailNotificationMessage> getNotificationMessageType() {
        return EmailNotificationMessage.class;
    }

    @Override
    public Object sendNotificationMessage(EmailNotificationRecipient<?> recipient, EmailNotificationMessage message) {
        Transport transport;
        try {
            transport = transports.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            if (!transport.isConnected()) {
                try {
                    if (config.auth) {
                        transport.connect(config.user, config.password);
                    } else {
                        transport.connect();
                    }
                } catch (IllegalStateException e) {
                    // Only rethrow when we are still not connected
                    if (!transport.isConnected()) {
                        throw e;
                    }
                }
            }

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

            if (config.getFilter() == null || config.getFilter().filterSmtpMessage(recipient, message, msg)) {
                transport.sendMessage(msg, new InternetAddress[]{new InternetAddress(recipient.getEmail())});
                String messageId;
                if (config.extractMessageId) {
                    String response = getResponse(transport);
                    messageId = response.substring(response.lastIndexOf(' ') + 1).trim();
                } else {
                    messageId = msg.getMessageID();
                }
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "SMTP notification sent to " + recipient + " with message id: " + messageId);
                }
                return messageId;
            } else if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "SMTP notification to " + recipient + " skipped by filter");
            }
            return null;
        } catch (SendFailedException e) {
            Integer responseCode = getResponseCode(e);
            if (responseCode != null) {
                switch (responseCode) {
                    case 421: // Too many concurrent SMTP connections
                    case 451: // Temporary service failure
                        LOG.log(Level.FINE, "Temporary service failure", e);
                        throw new JobTemporaryException(e);
                    case 454: // Throttling failure
                        LOG.log(Level.FINE, "Rate limit exceeded", e);
                        // TODO: parse if daily or second rate limit and adapt wait time
                        throw new JobRateLimitException(e);
                }
            }
            LOG.log(Level.SEVERE, "Failed to send email", e);
            throw new NotificationException(e);
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Failed to send email", e);
            throw new NotificationException(e);
        } finally {
            transports.add(transport);
        }
    }

    private Integer getResponseCode(SendFailedException e) {
        if (e instanceof SMTPSendFailedException) {
            return ((SMTPSendFailedException) e).getReturnCode();
        }
        return null;
    }

    private String getResponse(Transport transport) {
        if (transport instanceof SMTPTransport) {
            return ((SMTPTransport) transport).getLastServerResponse();
        }
        return null;
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

    protected InternetAddress toInternetAddress(String email, String displayName) throws UnsupportedEncodingException, AddressException {
        if (email == null || "".equals(email.trim())) {
            throw new IllegalArgumentException("Please provide a valid address", null);
        }
        if (displayName == null || "".equals(displayName.trim())) {
            return new InternetAddress(email);
        }
        return new InternetAddress(email, displayName, "utf-8");
    }

    private void setupTruststore(Properties props) {
        TruststoreProvider truststoreProvider = loadTruststoreProvider();
        JSSETruststoreConfigurator configurator = new JSSETruststoreConfigurator(truststoreProvider);

        SSLSocketFactory factory = configurator.getSSLSocketFactory();
        if (factory != null) {
            props.put("mail.smtp.ssl.socketFactory", factory);
            if (configurator.getProvider().getPolicy() == HostnameVerificationPolicy.ANY) {
                props.setProperty("mail.smtp.ssl.trust", "*");
            }
        }
    }

    private TruststoreProvider loadTruststoreProvider() {
        Iterator<TruststoreProviderFactory> iter = ServiceLoader.load(TruststoreProviderFactory.class).iterator();
        return iter.hasNext() ? iter.next().create() : null;
    }

    public static class Config {
        private final String host;
        private final Integer port;
        private final boolean auth;
        private final String user;
        private final String password;
        private final boolean enableSsl;
        private final boolean enableStartTls;
        private final boolean extractMessageId;
        private final long timeout;
        private final long connectionTimeout;
        private final int connectionPoolSize;
        private final String nodeName;
        private final SmtpChannelFilter filter;

        Config(String host, Integer port, boolean auth, String user, String password, boolean enableSsl, boolean enableStartTls, boolean extractMessageId, long timeout, long connectionTimeout, int connectionPoolSize, String nodeName, SmtpChannelFilter filter) {
            this.host = host;
            this.port = port;
            this.auth = auth;
            this.user = user;
            this.password = password;
            this.enableSsl = enableSsl;
            this.enableStartTls = enableStartTls;
            this.extractMessageId = extractMessageId;
            this.timeout = timeout;
            this.connectionTimeout = connectionTimeout;
            this.connectionPoolSize = connectionPoolSize;
            this.nodeName = nodeName;
            this.filter = filter;
        }

        public String getHost() {
            return host;
        }

        public Integer getPort() {
            return port;
        }

        public boolean isAuth() {
            return auth;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public boolean isEnableSsl() {
            return enableSsl;
        }

        public boolean isEnableStartTls() {
            return enableStartTls;
        }

        public boolean isExtractMessageId() {
            return extractMessageId;
        }

        public long getTimeout() {
            return timeout;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public int getConnectionPoolSize() {
            return connectionPoolSize;
        }

        public String getNodeName() {
            return nodeName;
        }

        public SmtpChannelFilter getFilter() {
            return filter;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String host;
            private Integer port;
            private boolean auth;
            private String user;
            private String password;
            private boolean enableSsl;
            private boolean enableStartTls;
            private boolean extractMessageId;
            private long timeout = 10000;
            private long connectionTimeout = 10000;
            private int connectionPoolSize = 1;
            private String nodeName;
            private SmtpChannelFilter filter;

            public Config build() {
                return new Config(host, port, auth, user, password, enableSsl, enableStartTls, extractMessageId, timeout, connectionTimeout, connectionPoolSize, nodeName, filter);
            }

            public Builder withHost(String host) {
                this.host = host;
                return this;
            }

            public Builder withPort(Integer port) {
                this.port = port;
                return this;
            }

            public Builder withAuth(String user, String password) {
                this.auth = true;
                this.user = user;
                this.password = password;
                return this;
            }

            public Builder withEnableSsl(boolean enableSsl) {
                this.enableSsl = enableSsl;
                return this;
            }

            public Builder withEnableStartTls(boolean enableStartTls) {
                this.enableStartTls = enableStartTls;
                return this;
            }

            public Builder withExtractMessageId(boolean extractMessageId) {
                this.extractMessageId = extractMessageId;
                return this;
            }

            public Builder withTimeout(long timeout) {
                this.timeout = timeout;
                return this;
            }

            public Builder withConnectionTimeout(long connectionTimeout) {
                this.connectionTimeout = connectionTimeout;
                return this;
            }

            public Builder withConnectionPoolSize(int connectionPoolSize) {
                this.connectionPoolSize = connectionPoolSize;
                return this;
            }

            public Builder withNodeName(String nodeName) {
                this.nodeName = nodeName;
                return this;
            }

            public Builder withFilter(SmtpChannelFilter filter) {
                this.filter = filter;
                return this;
            }
        }
    }
}
