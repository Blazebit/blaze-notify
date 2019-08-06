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

import com.blazebit.notify.domain.declarative.DiscoverMode;
import com.blazebit.notify.domain.declarative.DomainAttribute;
import com.blazebit.notify.domain.declarative.DomainType;
import com.blazebit.notify.domain.declarative.persistence.EntityAttribute;
import com.blazebit.notify.domain.declarative.persistence.EntityType;
import com.blazebit.notify.notification.channel.smtp.SmtpNotificationRecipient;
import com.blazebit.notify.notification.jpa.model.base.AbstractNotificationRecipient;

import javax.persistence.*;

@Entity
@SequenceGenerator(name = "idGenerator", sequenceName = "email_notification_recipient_seq", allocationSize = 1)
@Table(name = "email_notification_recipient")
@DomainType(discoverMode = DiscoverMode.EXPLICIT)
@EntityType(EmailNotificationRecipient.class)
public class EmailNotificationRecipient extends AbstractNotificationRecipient implements SmtpNotificationRecipient<Long> {

    private String email;

    public EmailNotificationRecipient() {
    }

    public EmailNotificationRecipient(Long id) {
        super(id);
    }

    @Override
    @DomainAttribute(Integer.class)
    @EntityAttribute("id")
    public Long getId() {
        return super.getId();
    }

    @Override
    @Column(nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
