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

import com.blazebit.notify.actor.ActorContext;
import com.blazebit.notify.actor.ConsumeContext;
import com.blazebit.notify.actor.ConsumingActor;
import com.blazebit.notify.actor.spi.ConsumerListener;

import java.util.List;

public class ConsumerListenerImpl<T> implements ConsumerListener<T> {

    private final ActorContext context;
    private final ConsumingActor<T> consumingActor;

    public ConsumerListenerImpl(ActorContext context, ConsumingActor<T> consumingActor) {
        this.context = context;
        this.consumingActor = consumingActor;
    }

    @Override
    public void consume(List<T> messages) {
        consumingActor.work(new ConsumeContextImpl<>(messages));
    }

    static class ConsumeContextImpl<T> implements ConsumeContext<T> {

        private final List<T> batch;

        public ConsumeContextImpl(List<T> batch) {
            this.batch = batch;
        }

        @Override
        public List<T> getBatch() {
            return batch;
        }
    }
}
