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

import com.blazebit.notify.jpa.model.base.AbstractTriggerBasedNotificationJobInstance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@SequenceGenerator(name = "idGenerator", sequenceName = "job_instance_seq", allocationSize = 1)
public class EmailNotificationJobInstance extends AbstractTriggerBasedNotificationJobInstance<Long, Long, EmailNotificationJob, EmailNotificationJobTrigger> {

    public EmailNotificationJobInstance() {
    }

    public EmailNotificationJobInstance(Long id) {
        super(id);
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
    public Long getId() {
        return id();
    }
}
