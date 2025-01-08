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

import com.blazebit.notify.ConfigurationSourceProvider;

import com.blazebit.notify.NotificationRecipient;
import com.blazebit.notify.email.message.EmailNotificationRecipient;
import com.blazebit.notify.email.model.jpa.AbstractEmailNotification;
import com.blazebit.notify.email.model.jpa.ColumnTypes;
import java.util.Locale;
import java.util.TimeZone;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * A simple E-Mail notification.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@SequenceGenerator(name = "idGenerator", sequenceName = "email_notification_seq")
@Table(name = "email_notification")
public class EmailNotification extends AbstractEmailNotification<Long> implements ConfigurationSourceProvider {

    private static final long serialVersionUID = 1L;

    private String to;
    private FromEmail from;

    /**
     * Creates an empty E-Mail notification.
     */
    public EmailNotification() {
        super();
    }

    /**
     * Creates a E-Mail notification with the given id.
     *
     * @param id The notification id
     */
    public EmailNotification(Long id) {
        super(id);
    }

    @Id
    @Override
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
    public Long getId() {
        return id();
    }

    @Override
    @Transient
    public Long getPartitionKey() {
        return id();
    }

    /**
     * Returns the to E-Mail address.
     *
     * @return the to E-Mail address
     */
    @NotNull
    @Column(name = "to_email", nullable = false, columnDefinition = ColumnTypes.MAIL_RECIPIENT)
    public String getTo() {
        return to;
    }

    /**
     * Sets the to E-Mail address.
     *
     * @param to The to E-Mail address
     */
    public void setTo(String to) {
        this.to = to;
    }

    @Override
    @Transient
    public NotificationRecipient getRecipient() {
        return EmailNotificationRecipient.of(to, Locale.getDefault(), TimeZone.getDefault(), to);
    }

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_email", nullable = false)
    @Override
    public FromEmail getFrom() {
        return from;
    }

    public void setFrom(FromEmail from) {
        this.from = from;
    }
}
