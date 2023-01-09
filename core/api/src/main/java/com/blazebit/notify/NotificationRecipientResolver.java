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
package com.blazebit.notify;

import com.blazebit.job.JobInstanceProcessingContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A resolver for determining the recipients for notifications that should be generated for a {@link NotificationJobInstance}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationRecipientResolver {

    /**
     * Resolves the notification recipients for which notifications should be generated for.
     *
     * @param jobInstance The notification job instance
     * @param jobProcessingContext The job instance processing context
     * @return The list of resolved notification recipients
     */
    List<? extends NotificationRecipient<?>> resolveNotificationRecipients(NotificationJobInstance<Long, ?> jobInstance, JobInstanceProcessingContext<?> jobProcessingContext);

    /**
     * Returns a {@link NotificationRecipientResolver} that statically always resolves the given recipients.
     *
     * @param recipients The recipients to resolve statically
     * @param <X> The recipient type
     * @return a {@link NotificationRecipientResolver}
     */
    static <X extends NotificationRecipient<?>> NotificationRecipientResolver of(X... recipients) {
        final List<X> list = new ArrayList<>(recipients.length);
        Collections.addAll(list, recipients);
        return new NotificationRecipientResolver() {
            @Override
            public List<X> resolveNotificationRecipients(NotificationJobInstance<Long, ?> jobInstance, JobInstanceProcessingContext<?> jobProcessingContext) {
                return list;
            }
        };
    }
}
