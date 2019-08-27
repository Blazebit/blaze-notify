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

package com.blazebit.notify.job;

public class JobRateLimitException extends JobTemporaryException {

    public JobRateLimitException() {
    }

    public JobRateLimitException(String message) {
        super(message);
    }

    public JobRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobRateLimitException(Throwable cause) {
        super(cause);
    }

    public JobRateLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public JobRateLimitException(long deferMillis) {
        super(deferMillis);
    }

    public JobRateLimitException(String message, long deferMillis) {
        super(message, deferMillis);
    }

    public JobRateLimitException(String message, Throwable cause, long deferMillis) {
        super(message, cause, deferMillis);
    }

    public JobRateLimitException(Throwable cause, long deferMillis) {
        super(cause, deferMillis);
    }

    public JobRateLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, long deferMillis) {
        super(message, cause, enableSuppression, writableStackTrace, deferMillis);
    }
}
