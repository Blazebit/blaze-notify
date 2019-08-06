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
package com.blazebit.notify.job.transaction.javaee;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.exception.ExceptionUtils;
import com.blazebit.notify.job.JobContext;
import com.blazebit.notify.job.spi.TransactionSupport;

import javax.transaction.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@ServiceProvider(TransactionSupport.class)
public class JtaTransactionSupport implements TransactionSupport {

    private final TransactionManager tm;
    private final TransactionSynchronizationRegistry tsr;

    public JtaTransactionSupport() {
        this(JtaResources.getInstance());
    }

    private JtaTransactionSupport(JtaResources jtaResources) {
        this(jtaResources.getTransactionManager(), jtaResources.getTransactionSynchronizationRegistry());
    }

    public JtaTransactionSupport(TransactionManager tm) {
        this(tm, (TransactionSynchronizationRegistry) tm);
    }

    public JtaTransactionSupport(TransactionManager tm, TransactionSynchronizationRegistry tsr) {
        if (tm == null) {
            throw new IllegalArgumentException("No TransactionManager given!");
        }
        if (tsr == null) {
            throw new IllegalArgumentException("No TransactionSynchronizationRegistry given!");
        }
        this.tm = tm;
        this.tsr = tsr;
    }

    @Override
    public <T> T transactional(JobContext context, long transactionTimeoutMillis, boolean joinIfPossible, Callable<T> callable, Consumer<Throwable> exceptionHandler) {
        boolean ourTx = false;
        Transaction tx = null;
        Transaction suspendedTx = null;
        Throwable exception = null;
        try {
            int status = tm.getStatus();
            if (joinIfPossible && allOk(status)) {
                tx = tm.getTransaction();
            } else {
                ourTx = true;
                tm.setTransactionTimeout((int) TimeUnit.MILLISECONDS.toSeconds(transactionTimeoutMillis));
                if (status == Status.STATUS_NO_TRANSACTION) {
                    tm.begin();
                    tx = tm.getTransaction();
                } else {
                    suspendedTx = tm.suspend();
                    tm.begin();
                    tx = tm.getTransaction();
                }
            }

            return callable.call();
        } catch (Throwable t) {
            exception = t;
            if (tx != null) {
                try {
                    tx.setRollbackOnly();
                } catch (Throwable e) {
                    t.addSuppressed(e);
                }
            }
            if (ourTx) {
                exceptionHandler.accept(t);
            } else {
                ExceptionUtils.doThrow(t);
            }
            return null;
        } finally {
            if (ourTx) {
                try {
                    if (tx != null) {
                        switch (tx.getStatus()) {
                            case Status.STATUS_ACTIVE:
                            case Status.STATUS_PREPARED:
                            case Status.STATUS_COMMITTED:
                            case Status.STATUS_UNKNOWN:
                            case Status.STATUS_NO_TRANSACTION:
                            case Status.STATUS_PREPARING:
                            case Status.STATUS_COMMITTING:
                                tm.commit();
                                break;
                            case Status.STATUS_MARKED_ROLLBACK:
                            case Status.STATUS_ROLLEDBACK:
                            case Status.STATUS_ROLLING_BACK:
                                tm.rollback();
                                break;
                        }
                    }
                } catch (Throwable e) {
                    // Only handle errors when we didn't already had an error before
                    if (exception == null) {
                        exceptionHandler.accept(e);
                    } else {
                        exception.addSuppressed(e);
                    }
                } finally {
                    if (suspendedTx != null) {
                        try {
                            tm.resume(suspendedTx);
                        } catch (Throwable e) {
                            // TODO: Maybe just log this? The parent handler will handle issues with the suspended tx anyway
                            // For now, let's just ignore resume errors here since parent handlers will run into exceptions anyway
//                            if (exception == null) {
//                                exceptionHandler.accept(e);
//                            } else {
//                                exception.addSuppressed(e);
//                            }
                        }
                    }
                }
            }
        }
    }

    private boolean allOk(int status) {
        switch (status) {
            case Status.STATUS_MARKED_ROLLBACK:
            case Status.STATUS_NO_TRANSACTION:
            case Status.STATUS_ROLLEDBACK:
            case Status.STATUS_ROLLING_BACK:
            case Status.STATUS_UNKNOWN:
                return false;
        }

        return true;
    }

    @Override
    public void registerPostCommitListener(Runnable o) {
        try {
            if (tm.getStatus() == Status.STATUS_NO_TRANSACTION) {
                throw new IllegalStateException("No active transaction!");
            }
            tsr.registerInterposedSynchronization(new Synchronization() {
                @Override
                public void beforeCompletion() {
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == Status.STATUS_COMMITTED) {
                        o.run();
                    }
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
