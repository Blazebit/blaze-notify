/*
 * Copyright 2018 - 2022 Blazebit.
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

package com.blazebit.notify.jpa.model.expression;

import com.blazebit.notify.jpa.model.base.AbstractNotificationJobInstance;

import javax.persistence.MappedSuperclass;

/**
 * An extension of {@link AbstractNotificationJobInstance} that contains a Blaze-Expression expression to select recipients.
 *
 * @param <R> The recipient cursor type
 * @author Christian Beikov
 * @since 1.0.0
 */
@MappedSuperclass
public abstract class AbstractExpressionAwareNotificationJobInstance<R> extends AbstractNotificationJobInstance<R> {

    private static final long serialVersionUID = 1L;

    private String recipientExpression;

    /**
     * Creates an empty expression aware notification job instance.
     */
    public AbstractExpressionAwareNotificationJobInstance() {
    }

    /**
     * Creates am expression aware notification job instance with the given id.
     *
     * @param id The expression aware notification job instance id
     */
    public AbstractExpressionAwareNotificationJobInstance(Long id) {
        super(id);
    }

    /**
     * Returns the expression to use for selecting recipients.
     *
     * @return the expression to use for selecting recipients
     */
    public String getRecipientExpression() {
        return recipientExpression;
    }

    /**
     * Sets the recipient expression.
     *
     * @param recipientExpression The recipient expression
     */
    public void setRecipientExpression(String recipientExpression) {
        this.recipientExpression = recipientExpression;
    }
}
