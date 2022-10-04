/*
 * Copyright 2018 - 2022 Blazebit.
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

package com.blazebit.notify.server.model;

import com.blazebit.notify.email.model.jpa.AbstractEmailNotification;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
public class JobBasedEmailNotification extends AbstractEmailNotification<JobBasedEmailNotificationId> {

    private EmailNotificationRecipient recipient;
    private EmailNotificationJobInstance notificationJobInstance;
    private Long recipientId;
    private Long notificationJobInstanceId;

    public JobBasedEmailNotification() {
        super(new JobBasedEmailNotificationId());
    }

    public JobBasedEmailNotification(JobBasedEmailNotificationId id) {
        super(id);
    }

    @Override
    @Transient
    public Long getPartitionKey() {
        return getRecipientId();
    }

    @Override
    @EmbeddedId
    public JobBasedEmailNotificationId getId() {
        return id();
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", insertable = false, updatable = false, nullable = false)
    public EmailNotificationRecipient getRecipient() {
        return recipient;
    }

    public void setRecipient(EmailNotificationRecipient recipient) {
        this.recipient = recipient;
        if (recipient == null) {
            id().setRecipientId(null);
        } else {
            id().setRecipientId(recipient.getId());
        }
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_job_instance_id", insertable = false, updatable = false, nullable = false)
    public EmailNotificationJobInstance getNotificationJobInstance() {
        return notificationJobInstance;
    }

    public void setNotificationJobInstance(EmailNotificationJobInstance notificationJobInstance) {
        this.notificationJobInstance = notificationJobInstance;
        if (notificationJobInstance == null) {
            id().setNotificationJobInstanceId(null);
        } else {
            id().setNotificationJobInstanceId(notificationJobInstance.getId());
        }
    }

    @Column(name = "recipient_id", nullable = false, insertable = false, updatable = false)
    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    @Column(name = "notification_job_instance_id", nullable = false, insertable = false, updatable = false)
    public Long getNotificationJobInstanceId() {
        return notificationJobInstanceId;
    }

    public void setNotificationJobInstanceId(Long notificationJobInstanceId) {
        this.notificationJobInstanceId = notificationJobInstanceId;
    }
}
