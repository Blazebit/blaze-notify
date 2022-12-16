/*
 * Copyright 2018 - 2023 Blazebit.
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

import com.blazebit.notify.email.model.jpa.AbstractFromEmail;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "from_email")
@SequenceGenerator(name = "idGenerator", sequenceName = "from_email_seq")
public class FromEmail extends AbstractFromEmail {

    /**
     * Creates an empty {@link FromEmail} entity.
     */
    public FromEmail() {
    }

    /**
     * Creates {@link FromEmail} entity for the given id.
     *
     * @param id The id
     */
    public FromEmail(Long id) {
        super(id);
    }

    /**
     * Creates {@link FromEmail} entity for the given E-Mail address.
     *
     * @param email The E-Mail address
     */
    public FromEmail(String email) {
        super(email);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
    @Override
    public Long getId() {
        return id();
    }
}
