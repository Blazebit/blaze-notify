package com.blazebit.notify.notification.testsuite;

import com.blazebit.notify.notification.*;
import com.blazebit.notify.predicate.model.Predicate;
import com.blazebit.notify.template.api.Template;

public class SimpleNotificationJob implements NotificationJob<SimpleNotification<SimpleNotificationMessage>, SimpleNotificationMessage> {

    private final Channel<SimpleNotification<SimpleNotificationMessage>, SimpleNotificationMessage> channel;
    private final Template template;
    private final Schedule schedule;
    private final Schedule notificationSchedule;
    private final Predicate selectorPredicate;

    public SimpleNotificationJob(Channel<SimpleNotification<SimpleNotificationMessage>, SimpleNotificationMessage> channel, Schedule schedule, Schedule notificationSchedule) {
        this.channel = channel;
        this.schedule = schedule;
        this.notificationSchedule = notificationSchedule;
        this.template = null;
        this.selectorPredicate = null;
    }

    public SimpleNotificationJob(Channel<SimpleNotification<SimpleNotificationMessage>, SimpleNotificationMessage> channel, Template template, Schedule schedule, Schedule notificationSchedule, Predicate selectorPredicate) {
        this.channel = channel;
        this.template = template;
        this.schedule = schedule;
        this.notificationSchedule = notificationSchedule;
        this.selectorPredicate = selectorPredicate;
    }

    @Override
    public Channel<SimpleNotification<SimpleNotificationMessage>, SimpleNotificationMessage> getChannel() {
        return channel;
    }

    @Override
    public Template getTemplate() {
        return template;
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public Schedule getNotificationSchedule() {
        return notificationSchedule;
    }

    @Override
    public Predicate getSelectorPredicate() {
        return selectorPredicate;
    }
}
