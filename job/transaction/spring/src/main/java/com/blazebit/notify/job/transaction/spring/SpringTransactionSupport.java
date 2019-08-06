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
package com.blazebit.notify.job.transaction.spring;

import com.blazebit.notify.job.JobContext;
import com.blazebit.notify.job.spi.TransactionSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SpringTransactionSupport implements TransactionSupport {

    private final ThreadLocal<TransactionStack> transactionStackThreadLocal = new ThreadLocal<>();
    private final PlatformTransactionManager tm;

    public SpringTransactionSupport(PlatformTransactionManager tm) {
        this.tm = tm;
    }

    @Override
    public <T> T transactional(JobContext context, long transactionTimeoutMillis, boolean joinIfPossible, Callable<T> callable, Consumer<Throwable> exceptionHandler) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(tm);
        transactionTemplate.setTimeout((int) TimeUnit.MILLISECONDS.toSeconds(transactionTimeoutMillis));
        TransactionStack transactionStack = transactionStackThreadLocal.get();
        boolean root = false;
        if (transactionStack == null) {
            transactionStack = new TransactionStack();
            root = true;
            transactionStackThreadLocal.set(transactionStack);
        }
        if (joinIfPossible && allOk(transactionStack.transactionDefinitions)) {
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        } else {
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        }
        int index = transactionStack.transactionDefinitions.size();
        transactionStack.transactionDefinitions.add(transactionTemplate);
        boolean success = true;
        try {
            return transactionTemplate.execute(status -> {
                try {
                    return callable.call();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ThrowableWrapper(t);
                }
            });
        } catch (Throwable t) {
            success = false;
            if (t instanceof ThrowableWrapper) {
                exceptionHandler.accept(t.getCause());
            } else {
                exceptionHandler.accept(t);
            }
            return null;
        } finally {
            if (root) {
                transactionStackThreadLocal.remove();
                if (success) {
                    for (Runnable postCommitListener : transactionStack.postCommitListeners) {
                        postCommitListener.run();
                    }
                }
            } else {
                transactionStack.transactionDefinitions.remove(index);
            }
        }
    }

    static class ThrowableWrapper extends RuntimeException {
        public ThrowableWrapper(Throwable cause) {
            super(cause);
        }
    }

    static class TransactionStack {
        final List<TransactionDefinition> transactionDefinitions = new ArrayList<>();
        final List<Runnable> postCommitListeners = new ArrayList<>();
    }

    private boolean allOk(List<TransactionDefinition> transactionDefinitions) {
        for (int i = 0; i < transactionDefinitions.size(); i++) {
            TransactionDefinition transactionDefinition = transactionDefinitions.get(i);
            if (tm.getTransaction(transactionDefinition).isRollbackOnly()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void registerPostCommitListener(Runnable o) {
        TransactionStack transactionStack = transactionStackThreadLocal.get();
        if (transactionStack == null) {
            throw new IllegalStateException("No active transaction!");
        }
        transactionStack.postCommitListeners.add(o);
    }
}
