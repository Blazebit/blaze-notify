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
package com.blazebit.notify.job.spi;

import com.blazebit.notify.job.JobContext;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public interface TransactionSupport {

    TransactionSupport NOOP = new TransactionSupport() {
        @Override
        public <T> T transactional(JobContext context, long transactionTimeoutMillis, boolean joinIfPossible, Callable<T> callable, Consumer<Throwable> exceptionHandler) {
            try {
                return callable.call();
            } catch (Throwable t) {
                exceptionHandler.accept(t);
                return null;
            }
        }

        @Override
        public void registerPostCommitListener(Runnable o) {
            o.run();
        }
    };

    <T> T transactional(JobContext context, long transactionTimeoutMillis, boolean joinIfPossible, Callable<T> callable, Consumer<Throwable> exceptionHandler);

    void registerPostCommitListener(Runnable o);
}
