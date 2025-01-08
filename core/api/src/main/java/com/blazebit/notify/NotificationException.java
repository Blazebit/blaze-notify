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

package com.blazebit.notify;

import com.blazebit.job.JobException;

/**
 * An exception thrown by notification jobs or the notification job runtime.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class NotificationException extends JobException {

    /**
     * Creates a new exception.
     */
    public NotificationException() {
    }

    /**
     * Creates a new exception.
     *
     * @param message The message
     */
    public NotificationException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     *
     * @param cause The cause
     * @param message The message
     */
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     *
     * @param cause The cause
     */
    public NotificationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception.
     *
     * @param cause The cause
     * @param message The message
     * @param enableSuppression Whether to enable suppression
     * @param writableStackTrace Whether to create a writable stacktrace
     */
    public NotificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
