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
package com.blazebit.notify.job.transaction.jpa;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.notify.job.JobContext;
import com.blazebit.notify.job.spi.TransactionSupport;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@ServiceProvider(TransactionSupport.class)
public class JpaTransactionSupport implements TransactionSupport {

    private final ThreadLocal<TransactionStack> transactionStackThreadLocal = new ThreadLocal<>();

    @Override
    public <T> T transactional(JobContext context, long transactionTimeoutMillis, boolean joinIfPossible, Callable<T> callable, Consumer<Throwable> exceptionHandler) {
        EntityManager entityManager = context.getService(EntityManager.class);
        EntityTransaction entityTransaction = entityManager.getTransaction();
        TransactionStack transactionStack = transactionStackThreadLocal.get();
        boolean root = false;
        int index = -1;
        if (transactionStack == null) {
            transactionStack = new TransactionStack();
            transactionStack.entityTransactions.add(entityTransaction);
            entityTransaction.begin();
            root = true;
            transactionStackThreadLocal.set(transactionStack);
        } else if (!joinIfPossible || transactionStack.entityTransactions.get(0).getRollbackOnly()) {
            entityManager = entityManager.getEntityManagerFactory().createEntityManager();
            index = transactionStack.entityTransactions.size();
            transactionStack.entityTransactions.add(entityTransaction = entityManager.getTransaction());
            entityTransaction.begin();
        }

        boolean success = true;
        try {
            return callable.call();
        } catch (Throwable t) {
            success = false;
            exceptionHandler.accept(t);
            return null;
        } finally {
            if (root) {
                transactionStackThreadLocal.remove();
                if (success) {
                    entityTransaction.commit();
                    for (Runnable postCommitListener : transactionStack.postCommitListeners) {
                        postCommitListener.run();
                    }
                } else {
                    entityTransaction.rollback();
                }
            } else if (index != -1) {
                transactionStack.entityTransactions.remove(index);
                if (success) {
                    entityTransaction.commit();
                } else {
                    entityTransaction.rollback();
                }
            }
        }
    }

    static class TransactionStack {
        final List<EntityTransaction> entityTransactions = new ArrayList<>();
        final List<Runnable> postCommitListeners = new ArrayList<>();
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
