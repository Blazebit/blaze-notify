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

package com.blazebit.notify.email.model.jpa;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The string has to be a well-formed email address. Exact semantics of what makes up a valid email address are left to Bean Validation providers. Accepts CharSequence.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
public @interface Email {

    /**
     * Returns the message key to render for errors.
     *
     * @return the message key
     */
    String message() default "{com.blazebit.notify.validation.constraints.Email.message}";

    /**
     * Returns the validation groups for which this constraint should be checked.
     *
     * @return the validation groups for which this constraint should be checked
     */
    Class<?>[] groups() default {};

    /**
     * Returns the constraint payload.
     *
     * @return the constraint payload
     */
    Class<? extends Payload>[] payload() default {};
}
