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

package com.blazebit.notify.actor.declarative;

import com.blazebit.notify.actor.declarative.spi.DeclarativeActorContextBuilderProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class DeclarativeActor {

    private DeclarativeActor() {
    }

    public static DeclarativeActorContextBuilderProvider getDefaultProvider() {
        ServiceLoader<DeclarativeActorContextBuilderProvider> serviceLoader = ServiceLoader.load(DeclarativeActorContextBuilderProvider.class);
        Iterator<DeclarativeActorContextBuilderProvider> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        }

        throw new IllegalStateException("No DeclarativeActorContextBuilderProvider found on the class path. Please check if a valid implementation is on the class path.");
    }


}