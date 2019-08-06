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

import com.blazebit.notify.notification.channel.smtp.james.module.MessageSearchIndexModule;
import com.blazebit.notify.notification.channel.smtp.util.ImapMailClient;
import com.google.inject.Binder;
import com.google.inject.Module;
import org.apache.james.GuiceJamesServer;
import org.apache.james.modules.server.JMXServerModule;
import org.apache.james.server.core.configuration.Configuration;
import org.apache.james.user.memory.MemoryUsersRepository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.apache.james.MemoryJamesServerMain.IN_MEMORY_SERVER_AGGREGATE_MODULE;

public abstract class AbstractSmtpChannelIntegrationTest {
    private final static String TEST_MAIL_SERVER_DOMAIN = "localhost";
    protected final static String TEST_MAIL_USER = "blaze-notify-test@" + TEST_MAIL_SERVER_DOMAIN;
    private final static String TEST_MAIL_PWD = "blaze-notify-test";
    private final static int TEST_MAIL_SERVER_SMTP_PORT = 1025;
    private final static int TEST_MAIL_SERVER_IMAP_PORT = 1143;

    protected static final ImapMailClient mailClient = new ImapMailClient(TEST_MAIL_USER, TEST_MAIL_PWD, TEST_MAIL_SERVER_DOMAIN, TEST_MAIL_SERVER_IMAP_PORT, false);

    protected static SmtpChannel smtpChannel;
    private static GuiceJamesServer jamesServer;

    @BeforeClass
    public static void setup() {
        setupSmtpChannel();
        startMailServer();
    }

    @AfterClass
    public static void tearDown() {
        stopMailServer();
    }

    @Before
    public void connectMailClient() {
        mailClient.connect();
    }

    @After
    public void disconnectMailClient() {
        mailClient.markAllMessagesAsDeleted();
        mailClient.disconnect(true);
    }

    private static void setupSmtpChannel() {
        SmtpChannel.Config config = SmtpChannel.Config.builder()
                .withHost(TEST_MAIL_SERVER_DOMAIN)
                .withPort(TEST_MAIL_SERVER_SMTP_PORT)
                .build();
        smtpChannel = new SmtpChannel(config);
    }

    private static void startMailServer() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Configuration configuration = Configuration.builder().configurationFromClasspath().workingDirectory(tempDir).build();
        final MemoryUsersRepository jamesUsersRepository = MemoryUsersRepository.withVirtualHosting();
        jamesServer = GuiceJamesServer.forConfiguration(configuration).combineWith(
                IN_MEMORY_SERVER_AGGREGATE_MODULE,
                new JMXServerModule(),
                new MessageSearchIndexModule()
        ).overrideWith(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(MemoryUsersRepository.class).toInstance(jamesUsersRepository);

            }
        });
        try {
            jamesServer.start();
            jamesUsersRepository.addUser(TEST_MAIL_USER, TEST_MAIL_PWD);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void stopMailServer() {
        if (jamesServer != null && jamesServer.isStarted()) {
            jamesServer.stop();
        }
    }
}
