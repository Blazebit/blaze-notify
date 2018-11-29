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
package com.blazebit.notify.notification.scheduler.timer;

import com.blazebit.notify.notification.Scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceScheduler implements Scheduler {

    private final ScheduledExecutorService executorService;

    public ExecutorServiceScheduler() {
        this(Executors.newSingleThreadScheduledExecutor());
    }

    public ExecutorServiceScheduler(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, long schedule) {
        return executorService.schedule(task, schedule - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
}
