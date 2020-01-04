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
package com.blazebit.notify.channel.smtp.util;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

public class ImapMailClient extends Authenticator {
    private final Session session;
    private final String user;
    private final String password;

    private Store store;
    private Folder inbox;

    public ImapMailClient(String user, String password, String host, int imapPort, boolean debug) {
        this.user = user;
        this.password = password;
        Properties props = new Properties();
        props.put("mail.user", user);
        props.put("mail.host", host);
        props.put("mail.debug", debug ? "true" : "false");
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.port", Integer.toString(imapPort));
        session = Session.getInstance(props, this);
    }

    public Message[] awaitIncomingEmails(final long timeout,
            final int emailCount)
            throws InterruptedException {
        try {
            Message[] msgs = inbox.getMessages();

            long t0 = System.currentTimeMillis();
            while (msgs.length < emailCount) {
                Thread.sleep(timeout / 10);
                if ((System.currentTimeMillis() - t0) > timeout) {
                    return new Message[0];
                }
                msgs = inbox.getMessages();
            }

            return msgs;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void markAllMessagesAsDeleted() {
        try {
            for (Message msg : inbox.getMessages()) {
                msg.setFlag(Flags.Flag.DELETED, true);
            }
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void connect() {
        try {
            store = session.getStore();
            store.connect(user, password);

            Folder root = store.getDefaultFolder();
            inbox = root.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect(final boolean expunge) {
        try {
            final Store store = inbox.getStore();
            inbox.close(expunge);
            store.close();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
