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

package com.blazebit.notify.actor.consumer.postgresql.listen;

import com.blazebit.notify.actor.spi.Consumer;
import com.blazebit.notify.actor.spi.ConsumerListener;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;

import javax.sql.DataSource;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractPgjdbcNgListenConsumer<T> implements Consumer<T>, Runnable {

    private static final Logger LOG = Logger.getLogger(AbstractPgjdbcNgListenConsumer.class.getName());
    private final List<ConsumerListener<T>> listeners = new CopyOnWriteArrayList<>();
    private final BlockingDeque<String> payloadQueue = new LinkedBlockingDeque<>();
    private final DataSource dataSource;
    private final String channelName;
    private final PGNotificationListener listener = new PGNotificationListener() {
        @Override
        public void notification(int processId, String channelName, String payload) {
            payloadQueue.add(payload);
        }
    };

    public AbstractPgjdbcNgListenConsumer(DataSource dataSource, String channelName) {
        this.dataSource = dataSource;
        this.channelName = channelName;
    }

    @Override
    public void registerListener(ConsumerListener<T> listener) {
        listeners.add(listener);
    }

    protected abstract List<T> convertPayload(List<String> payloads);

    protected boolean isDone() {
        return Thread.currentThread().isInterrupted();
    }

    @Override
    public void run() {
        List<String> payloadList = new ArrayList<>();
        List<String> unmodifiableList = Collections.unmodifiableList(payloadList);
        while (!isDone()) {
            try (PGConnection connection = dataSource.getConnection().unwrap(PGConnection.class)) {
                // The listener is notified in an IO thread and pushes to the payload queue
                connection.addNotificationListener(listener);
                try (Statement statement = connection.createStatement()) {
                    statement.execute("LISTEN " + channelName);
                }

                // This runnable is scheduled wherever the user wants and drains the queue to inform listeners
                while (!isDone()) {
                    // Blocked wait
                    String first = payloadQueue.takeFirst();
                    payloadList.add(first);
                    // When we found one element, drain the rest that is available
                    payloadQueue.drainTo(payloadList);
                    try {
                        List<T> messages = Collections.unmodifiableList(convertPayload(unmodifiableList));
                        payloadList.clear();
                        try {
                            listeners.forEach(l -> l.consume(messages));
                        } catch (Throwable t) {
                            LOG.log(Level.SEVERE, "Error in ConsumerListener", t);
                        }
                    } finally {
                        payloadList.clear();
                    }
                }
            } catch (InterruptedException e) {
                // Stop the loop when we were interrupted
                break;
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Error in Consumer", t);
            }
        }
    }
}
