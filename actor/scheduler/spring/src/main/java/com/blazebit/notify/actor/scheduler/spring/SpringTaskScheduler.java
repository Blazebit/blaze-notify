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
package com.blazebit.notify.actor.scheduler.spring;

import com.blazebit.notify.actor.ActorContext;
import com.blazebit.notify.actor.ActorException;
import com.blazebit.notify.actor.spi.Scheduler;
import org.springframework.scheduling.TaskScheduler;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class SpringTaskScheduler implements Scheduler {

    private static final String SCHEDULER_PROPERTY = "actor.scheduler.spring.task_scheduler";

    private final String name;
    private final TaskScheduler taskScheduler;

    public SpringTaskScheduler(ActorContext actorContext, String name) {
        this(name, getTaskScheduler(actorContext, name));
    }

    public SpringTaskScheduler(TaskScheduler taskScheduler) {
        this(null, taskScheduler);
    }

    public SpringTaskScheduler(String name, TaskScheduler taskScheduler) {
        if (taskScheduler == null) {
            throw new ActorException("No task scheduler given");
        }
        this.name = name;
        this.taskScheduler = taskScheduler;
    }

    private static TaskScheduler getTaskScheduler(ActorContext actorContext, String name) {
        Object scheduler = null;
        if (name != null) {
            scheduler = actorContext.getProperty(SCHEDULER_PROPERTY + "." + name);
        }
        if (scheduler == null) {
            scheduler = actorContext.getProperty(SCHEDULER_PROPERTY);
        } else if (scheduler instanceof TaskScheduler) {
            return (TaskScheduler) scheduler;
        } else {
            throw new ActorException("The object given via the property '" + SCHEDULER_PROPERTY + "." + name + "' should be a TaskScheduler but isn't: " + scheduler);
        }
        if (scheduler == null) {
            return actorContext.getService(TaskScheduler.class);
        } else if (scheduler instanceof TaskScheduler) {
            return (TaskScheduler) scheduler;
        } else {
            throw new ActorException("The object given via the property '" + SCHEDULER_PROPERTY + "' should be a TaskScheduler but isn't: " + scheduler);
        }
    }

    @Override
    public <T> Future<T> schedule(Callable<T> task, long delayMillis) {
        FutureTask<T> t = new FutureTask<>(task);
        taskScheduler.schedule(t, new Date(System.currentTimeMillis() + delayMillis));
        return t;
    }

    @Override
    public boolean supportsStop() {
        return false;
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
}
