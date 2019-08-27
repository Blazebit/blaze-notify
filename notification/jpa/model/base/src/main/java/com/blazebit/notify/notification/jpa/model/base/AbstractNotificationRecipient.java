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

package com.blazebit.notify.notification.jpa.model.base;

import com.blazebit.notify.job.jpa.model.BaseEntity;
import com.blazebit.notify.notification.NotificationRecipient;

import javax.persistence.*;
import java.util.Locale;

@MappedSuperclass
public abstract class AbstractNotificationRecipient extends BaseEntity<Long> implements NotificationRecipient<Long> {

    private Locale locale;

    public AbstractNotificationRecipient() {
    }

    public AbstractNotificationRecipient(Long id) {
        super(id);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
    public Long getId() {
        return id();
    }

    @Override
    @Column(nullable = false)
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
