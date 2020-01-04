/*
 * Copyright 2018 - 2020 Blazebit.
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
package com.blazebit.notify.email.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * An entity for the from address.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@SequenceGenerator(name = "idGenerator", sequenceName = "from_email_seq")
@Table(name = "from_email")
public class FromEmail extends BaseEntity<Long> {

    private String email;
    private String name;
    private String replyToEmail;
    private String replyToName;

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
        this.email = email;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
    public Long getId() {
        return id();
    }

    /**
     * Returns the E-Mail address.
     *
     * @return the E-Mail address
     */
    @NotNull
    @Column(name = "email", columnDefinition = ColumnTypes.MAIL_RECIPIENT, unique = true)
    public String getEmail() {
        return email;
    }

    /**
     * Sets the E-Mail address.
     *
     * @param email The E-Mail address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the display name.
     *
     * @return the display name
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * Sets the display name.
     *
     * @param name The display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the reply to E-Mail address.
     *
     * @return the reply to E-Mail address
     */
    @Column(name = "reply_to_email", columnDefinition = ColumnTypes.MAIL_RECIPIENT)
    public String getReplyToEmail() {
        return replyToEmail;
    }

    /**
     * Sets the reply to E-Mail address.
     *
     * @param replyToEmail The reply to E-Mail address
     */
    public void setReplyToEmail(String replyToEmail) {
        this.replyToEmail = replyToEmail;
    }

    /**
     * Returns the reply to display name.
     *
     * @return the reply to display name
     */
    @Column(name = "reply_to_name")
    public String getReplyToName() {
        return replyToName;
    }

    /**
     * Sets the reply to display name.
     *
     * @param replyToName The reply to display name
     */
    public void setReplyToName(String replyToName) {
        this.replyToName = replyToName;
    }
}
