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
package com.blazebit.notify.actor.impl;

import com.blazebit.notify.actor.*;
import com.blazebit.notify.actor.spi.ClusterStateManager;
import com.blazebit.notify.actor.spi.Scheduler;
import com.blazebit.notify.actor.spi.SchedulerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActorManagerImpl implements ActorManager {

    private static final Logger LOG = Logger.getLogger(ActorManagerImpl.class.getName());

    private final ActorContext actorContext;
    private final SchedulerFactory schedulerFactory;
    private final ClusterStateManager clusterStateManager;
    private final ConcurrentMap<String, ActorEntry> registeredActors = new ConcurrentHashMap<>();

    public ActorManagerImpl(ActorContext actorContext, Map<String, ScheduledActor> initialActors) {
        this.actorContext = actorContext;
        this.schedulerFactory = actorContext.getService(SchedulerFactory.class);
        this.clusterStateManager = actorContext.getService(ClusterStateManager.class);

        for (Map.Entry<String, ScheduledActor> entry : initialActors.entrySet()) {
            getOrRegisterActor(entry.getKey(), entry.getValue());
        }

        this.clusterStateManager.registerListener(ActorRescheduleEvent.class, e -> {
            // Reschedule without delay since this is a cluster event
            if (!rescheduleActorLocally(e.getActorName(), 0)) {
                // TODO: queue events and fire when registered?
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.warning("Dropping rescheduling event because actor is not registered: " + e.getActorName());
                }
            }
        });
    }

    @Override
    public void registerSuspendedActor(String name, ScheduledActor actor) {
        getOrRegisterActor(name, actor);
    }

    @Override
    public void registerActor(String name, ScheduledActor actor, long initialDelayMillis) {
        getOrRegisterActor(name, actor).reschedule(initialDelayMillis);
    }

    private ActorEntry getOrRegisterActor(String name, ScheduledActor actor) {
        return registeredActors.compute(name, (n, actorEntry) -> {
            if (actorEntry != null) {
                if (!actorEntry.actor.equals(actor)) {
                    throw new ActorException("An actor is already scheduled for the name '" + name + "'. Can't schedule: " + actor);
                }

                return actorEntry;
            } else {
                Scheduler scheduler = schedulerFactory.createScheduler(actorContext, name);
                return new ActorEntry(name, actor, scheduler, registeredActors);
            }
        });
    }

    @Override
    public void rescheduleActor(String name, long delayMillis) {
        if (!rescheduleActorLocally(name, delayMillis)) {
            throw new ActorException("No actor with the name '" + name + "' is registered!");
        }
        clusterStateManager.fireEventExcludeSelf(new ActorRescheduleEvent(name));
    }

    private boolean rescheduleActorLocally(String name, long delayMillis) {
        if (delayMillis < 0) {
            throw new ActorException("Invalid negative delay!");
        }
        ActorEntry actorEntry = registeredActors.get(name);
        if (actorEntry == null) {
            return false;
        }
        actorEntry.reschedule(delayMillis);
        return true;
    }

    @Override
    public void removeActor(String name) {
        ActorEntry actorEntry = registeredActors.get(name);
        if (actorEntry == null) {
            return;
        }

        actorEntry.cancel();
    }

    private static class ActorEntry implements Callable<Void> {

        private final String name;
        private final ScheduledActor actor;
        private final Scheduler scheduler;
        private final ConcurrentMap<String, ActorEntry> registeredActors;
        private final Lock running = new ReentrantLock();
        private final AtomicReference<Thread> waitingThread = new AtomicReference<>();
        private volatile boolean cancelled;

        public ActorEntry(String name, ScheduledActor actor, Scheduler scheduler, ConcurrentMap<String, ActorEntry> registeredActors) {
            this.name = name;
            this.actor = actor;
            this.scheduler = scheduler;
            this.registeredActors = registeredActors;
        }

        public void reschedule(long delayMillis) {
            if (!cancelled) {
                scheduler.schedule(this, delayMillis);
            }
        }

        public void cancel() {
            this.cancelled = true;
            registeredActors.remove(name, this);
        }

        @Override
        public Void call() {
            // For the rare case that the actor is scheduled multiple times, we protect ourselves by only allowing a single run
            if (!running.tryLock()) {
                // There may only be one thread waiting
                Thread thread = Thread.currentThread();
                if (waitingThread.compareAndSet(null, thread)) {
                    running.lock();
                    waitingThread.set(null);
                } else {
                    return null;
                }
            }
            try {
                if (!cancelled) {
                    ActorRunResult runResult = actor.work();
                    if (runResult.isReschedule()) {
                        reschedule(runResult.getDelayMillis());
                    } else if (runResult.isDone()) {
                        registeredActors.remove(name, this);
                    } else {
//                    runner.onSuspend();
                    }
                }
            } finally {
                running.unlock();
            }
            return null;
        }
    }
}
