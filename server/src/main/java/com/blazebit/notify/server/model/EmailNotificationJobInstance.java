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

import com.blazebit.notify.notification.jpa.model.base.AbstractNotificationJobInstance;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;

@Entity
@SequenceGenerator(name = "idGenerator", sequenceName = "job_instance_seq", allocationSize = 1)
public class EmailNotificationJobInstance extends AbstractNotificationJobInstance<Long, EmailNotificationJob, EmailNotificationJobTrigger> {

    public EmailNotificationJobInstance() {
    }

    public EmailNotificationJobInstance(Long id) {
        super(id);
    }
}
