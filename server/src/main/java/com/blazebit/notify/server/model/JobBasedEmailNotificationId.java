/*
 * Copyright 2018 - 2019 Blazebit.
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

import com.blazebit.notify.jpa.model.base.AbstractNotificationId;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
@Embeddable
public class JobBasedEmailNotificationId extends AbstractNotificationId<Long, Long> {

    public JobBasedEmailNotificationId() {
    }

    public JobBasedEmailNotificationId(Long notificationJobInstanceId, Long recipientId) {
        super(notificationJobInstanceId, recipientId);
    }

    @Override
    @Column(name = "notification_job_instance_id", nullable = false)
    public Long getNotificationJobInstanceId() {
        return super.getNotificationJobInstanceId();
    }

    @Override
    @Column(name = "recipient_id", nullable = false)
    public Long getRecipientId() {
        return super.getRecipientId();
    }
}
