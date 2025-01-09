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
package com.blazebit.notify.channel.smtp;

import com.blazebit.notify.channel.smtp.util.ImapMailClient;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.james.FakeSearchMailboxModule;
import org.apache.james.GuiceJamesServer;
import org.apache.james.MemoryJamesConfiguration;
import org.apache.james.MemoryJamesServerMain;
import org.apache.james.modules.server.JMXServerModule;
import org.apache.james.user.api.UsersRepositoryManagementMBean;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractSmtpChannelIntegrationTest {
    private final static String TEST_MAIL_SERVER_DOMAIN = "localhost";
    protected final static String TEST_MAIL_USER = "blaze-notify-test@" + TEST_MAIL_SERVER_DOMAIN;
    private final static String TEST_MAIL_PWD = "blaze-notify-test";
    private final static int TEST_MAIL_SERVER_SMTP_PORT = 1025;
    private final static int TEST_MAIL_SERVER_IMAP_PORT = 10143;

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
        MemoryJamesConfiguration configuration = MemoryJamesConfiguration.builder()
            .configurationFromClasspath()
            .workingDirectory(tempDir)
            .build();
        jamesServer = MemoryJamesServerMain.createServer(configuration)
            .combineWith(new FakeSearchMailboxModule(), new JMXServerModule());
        try {
            jamesServer.start();
            addUser(TEST_MAIL_USER, TEST_MAIL_PWD);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addUser(String email, String password) {
        try {
            String serverUrl = "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";
            String beanNameUser = "org.apache.james:type=component,name=usersrepository";

            MBeanServerConnection server =
                JMXConnectorFactory.connect(new JMXServiceURL(serverUrl)).getMBeanServerConnection();

            UsersRepositoryManagementMBean userBean =
                MBeanServerInvocationHandler.newProxyInstance(server, new ObjectName(beanNameUser),
                    UsersRepositoryManagementMBean.class, false);
            userBean.addUser(email, password);
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
