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

public class JobTemporaryException extends JobException {

    private final long deferMillis;

    public JobTemporaryException() {
        this.deferMillis = -1L;
    }

    public JobTemporaryException(String message) {
        super(message);
        this.deferMillis = -1L;
    }

    public JobTemporaryException(String message, Throwable cause) {
        super(message, cause);
        this.deferMillis = -1L;
    }

    public JobTemporaryException(Throwable cause) {
        super(cause);
        this.deferMillis = -1L;
    }

    public JobTemporaryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.deferMillis = -1L;
    }

    public JobTemporaryException(long deferMillis) {
        this.deferMillis = deferMillis;
    }

    public JobTemporaryException(String message, long deferMillis) {
        super(message);
        this.deferMillis = deferMillis;
    }

    public JobTemporaryException(String message, Throwable cause, long deferMillis) {
        super(message, cause);
        this.deferMillis = deferMillis;
    }

    public JobTemporaryException(Throwable cause, long deferMillis) {
        super(cause);
        this.deferMillis = deferMillis;
    }

    public JobTemporaryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, long deferMillis) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.deferMillis = deferMillis;
    }

    public long getDeferMillis() {
        return deferMillis;
    }
}
