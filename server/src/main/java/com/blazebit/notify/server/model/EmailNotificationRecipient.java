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

package com.blazebit.notify.server.model;

import com.blazebit.domain.declarative.DiscoverMode;
import com.blazebit.domain.declarative.DomainAttribute;
import com.blazebit.domain.declarative.DomainType;
import com.blazebit.notify.jpa.model.base.AbstractNotificationRecipient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@SequenceGenerator(name = "idGenerator", sequenceName = "email_notification_recipient_seq", allocationSize = 1)
@Table(name = "email_notification_recipient")
@DomainType(discoverMode = DiscoverMode.EXPLICIT)
public class EmailNotificationRecipient extends AbstractNotificationRecipient implements com.blazebit.notify.email.message.EmailNotificationRecipient<Long> {

    private String email;

    public EmailNotificationRecipient() {
    }

    public EmailNotificationRecipient(Long id) {
        super(id);
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
    @DomainAttribute(Integer.class)
    public Long getId() {
        return id();
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
