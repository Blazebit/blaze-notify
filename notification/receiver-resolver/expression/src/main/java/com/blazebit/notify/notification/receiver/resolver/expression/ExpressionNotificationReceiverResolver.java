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
package com.blazebit.notify.notification.receiver.resolver.expression;

import com.blazebit.notify.expression.ExpressionServiceFactory;
import com.blazebit.notify.expression.Predicate;
import com.blazebit.notify.notification.*;

import java.util.List;

public class ExpressionNotificationReceiverResolver<N extends Notification<T>, T extends NotificationMessage> implements NotificationReceiverResolver<N, T> {

    private final ExpressionServiceFactory expressionServiceFactory;
    private final Predicate predicate;

    public ExpressionNotificationReceiverResolver(ExpressionServiceFactory expressionServiceFactory, Predicate predicate) {
        this.expressionServiceFactory = expressionServiceFactory;
        this.predicate = predicate;
    }

    @Override
    public List<NotificationReceiver> resolveNotificationReceivers(NotificationJob<N, T> hob, NotificationJobContext jobContext) {
        // TODO
        // expressionServiceFactory.createInterpreter().evaluate(this.predicate, null);
        return null;
    }

    public Predicate getPredicate() {
        return predicate;
    }
}
