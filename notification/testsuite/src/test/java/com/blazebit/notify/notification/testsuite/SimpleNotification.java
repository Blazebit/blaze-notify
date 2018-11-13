package com.blazebit.notify.notification.testsuite;

import com.blazebit.notify.notification.Channel;
import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.NotificationMessage;
import com.blazebit.notify.notification.NotificationReceiver;

public class SimpleNotification<T extends NotificationMessage> implements Notification<T> {

    private final T message;
    private final Channel<Notification<T>, T> channel;
    private final NotificationReceiver receiver;
    private final long epochDeadline;

    public SimpleNotification(T message, Channel<Notification<T>, T> channel, NotificationReceiver receiver, long epochDeadline) {
        this.message = message;
        this.channel = channel;
        this.receiver = receiver;
        this.epochDeadline = epochDeadline;
    }

    @Override
    public T getMessage() {
        return message;
    }

    @Override
    public Channel<Notification<T>, T> getChannel() {
        return channel;
    }

    @Override
    public NotificationReceiver getReceiver() {
        return receiver;
    }

    @Override
    public long getEpochDeadline() {
        return epochDeadline;
    }
}
