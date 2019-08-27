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

package com.blazebit.notify.actor.consumer.sqs;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.blazebit.notify.actor.spi.Consumer;
import com.blazebit.notify.actor.spi.ConsumerListener;

import javax.jms.IllegalStateException;
import javax.jms.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConsumerListeners shouldn't acknowledge, this will happen in SqsConsumer in a batched way.
 */
public class SqsConsumer implements Consumer<Message>, Runnable {

    private static final Logger LOG = Logger.getLogger(SqsConsumer.class.getName());
    private final List<ConsumerListener<Message>> listeners = new CopyOnWriteArrayList<>();
    private final MessageConsumer messageConsumer;
    private volatile boolean closed;

    public SqsConsumer(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void registerListener(ConsumerListener<Message> listener) {
        listeners.add(listener);
    }

    protected boolean isDone() {
        return closed;
    }

    @Override
    public void run() {
        List<Message> messages = new ArrayList<>();
        List<Message> unmodifiableList = Collections.unmodifiableList(messages);
        while (!isDone()) {
            try {
                // Blocked wait
                Message msg = messageConsumer.receive();
                messages.add(msg);
                // When we found one element, drain the rest that is available
                // TODO: Maybe allow to configure a limit?
                while ((msg = messageConsumer.receiveNoWait()) != null) {
                    messages.add(msg);
                }
                try {
                    boolean error = true;
                    try {
                        listeners.forEach(l -> l.consume(unmodifiableList));
                        error = false;
                    } catch (Throwable t) {
                        LOG.log(Level.SEVERE, "Error in ConsumerListener", t);
                    } finally {
                        // We only acknowledge when no errors happened
                        if (!error) {
                            // We acknowledge the last message first so that the RANGE acknowledge mode can batch acknowledge messages
                            for (int i = messages.size() - 1; i >= 0; i--) {
                                messages.get(i).acknowledge();
                            }
                        }
                    }
                } finally {
                    messages.clear();
                }
            } catch (IllegalStateException e) {
                // This can only happen due to the message consumer being closed
                closed = true;
                // But make sure it's closed anyway
                LOG.log(Level.SEVERE, "Closing Consumer as the underlying MessageConsumer seems closed", e);
                try {
                    messageConsumer.close();
                } catch (Throwable ex) {
                    LOG.log(Level.SEVERE, "Error while closing MessageConsumer", ex);
                }
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Error in Consumer", t);
            }
        }
    }

    public static class Config {

        private final String accessKey;
        private final String secretKey;
        private final String region;
        private final String queueName;
        private final int prefetchSize;

        Config(String accessKey, String secretKey, String region, String queueName, int prefetchSize) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.region = region;
            this.queueName = queueName;
            this.prefetchSize = prefetchSize;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public String getRegion() {
            return region;
        }

        public String getQueueName() {
            return queueName;
        }

        public List<SqsConsumer> createConsumers(SQSConnection connection, int consumers) {
            List<SqsConsumer> consumerList = new ArrayList<>(consumers);
            for (int i = 0; i < consumers; i++) {
                consumerList.add(createConsumer(connection));
            }
            return consumerList;
        }

        public SqsConsumer createConsumer(SQSConnection connection) {
            Session session = null;
            try {
                session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                return new SqsConsumer(session.createConsumer(session.createQueue(queueName)));
            } catch (Throwable e) {
                if (session != null) {
                    try {
                        session.close();
                    } catch (Throwable ex) {
                        e.addSuppressed(ex);
                    }
                }
                throw new RuntimeException("Couldn't create consumer", e);
            }
        }

        public SQSConnection createConnection() {
            SQSConnectionFactory connectionFactory = createConnectionFactory();
            SQSConnection connection = null;
            try {
                return connectionFactory.createConnection();
            } catch (Throwable e) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (JMSException ex) {
                        e.addSuppressed(ex);
                    }
                }
                throw new RuntimeException("Couldn't create Connection", e);
            }
        }

        public SQSConnectionFactory createConnectionFactory() {
            AWSCredentials credentials;

            if (accessKey == null || secretKey == null) {
                throw new IllegalArgumentException("No AWS access key and secret key given for SQS queue!");
            } else {
                credentials = new BasicAWSCredentials(accessKey, secretKey);
            }

            if (region == null) {
                throw new IllegalArgumentException("No AWS region given for SQS queue!");
            }

            ProviderConfiguration providerConfiguration = new ProviderConfiguration();
            providerConfiguration.withNumberOfMessagesToPrefetch(prefetchSize);
            AmazonSQS sqs = AmazonSQSClient.builder()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(region)
                    .build();
            return new SQSConnectionFactory(providerConfiguration, sqs);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private static final int DEFAULT_PREFETCH_SIZE = 10;
            private String accessKey;
            private String secretKey;
            private String region;
            private String queueName;
            private int prefetchSize = DEFAULT_PREFETCH_SIZE;

            public Config build() {
                return new Config(accessKey, secretKey, region, queueName, prefetchSize);
            }

            public Builder withAccessKey(String accessKey) {
                this.accessKey = accessKey;
                return this;
            }

            public Builder withSecretKey(String secretKey) {
                this.secretKey = secretKey;
                return this;
            }

            public Builder withRegion(String region) {
                this.region = region;
                return this;
            }

            public Builder withQueueName(String queueName) {
                this.queueName = queueName;
                return this;
            }

            public Builder withPrefetchSize(int prefetchSize) {
                this.prefetchSize = prefetchSize;
                return this;
            }
        }
    }
}
