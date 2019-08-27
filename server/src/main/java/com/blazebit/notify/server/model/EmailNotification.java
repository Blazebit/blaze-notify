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

package com.blazebit.notify.server.model;

import com.blazebit.notify.notification.jpa.model.base.AbstractNotification;

import javax.persistence.*;

@Entity
@AssociationOverrides({
        @AssociationOverride(name = "recipient", joinColumns = @JoinColumn(name = "recipient_id", nullable = false, insertable = false, updatable = false)),
        @AssociationOverride(name = "notificationJobInstance", joinColumns = @JoinColumn(name = "notification_job_instance_id", nullable = false, insertable = false, updatable = false))
})
public class EmailNotification extends AbstractNotification<EmailNotificationId, EmailNotificationRecipient, EmailNotificationJobInstance> {

    private Long recipientId;
    private Long notificationJobInstanceId;

    public EmailNotification() {
        super(new EmailNotificationId());
    }

    public EmailNotification(EmailNotificationId id) {
        super(id);
    }

    @EmbeddedId
    @Override
    public EmailNotificationId getId() {
        return id();
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
