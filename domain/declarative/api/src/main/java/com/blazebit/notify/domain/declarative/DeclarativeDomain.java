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

package com.blazebit.notify.domain.declarative;

import com.blazebit.notify.domain.declarative.spi.DeclarativeDomainBuilderProvider;
import com.blazebit.notify.domain.spi.DomainBuilderProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class DeclarativeDomain {

    private DeclarativeDomain() {
    }

    public static DeclarativeDomainBuilderProvider getDefaultProvider() {
        ServiceLoader<DeclarativeDomainBuilderProvider> serviceLoader = ServiceLoader.load(DeclarativeDomainBuilderProvider.class);
        Iterator<DeclarativeDomainBuilderProvider> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        }

        throw new IllegalStateException("No DeclarativeDomainBuilderProvider found on the class path. Please check if a valid implementation is on the class path.");
    }


}
