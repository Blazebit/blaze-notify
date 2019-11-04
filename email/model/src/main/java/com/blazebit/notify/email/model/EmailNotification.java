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
package com.blazebit.notify.email.model;

import com.blazebit.notify.ConfigurationSourceProvider;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

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
}
