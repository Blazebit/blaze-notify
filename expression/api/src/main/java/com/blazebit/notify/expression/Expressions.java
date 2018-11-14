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

package com.blazebit.notify.expression;

import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.expression.spi.ExpressionServiceFactoryProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class Expressions {

    private Expressions() {
    }

    public static ExpressionServiceFactory forModel(DomainModel model) {
        return getDefaultProvider().create(model);
    }

    public static ExpressionServiceFactoryProvider getDefaultProvider() {
        Iterator<ExpressionServiceFactoryProvider> iterator = ServiceLoader.load(ExpressionServiceFactoryProvider.class).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }

        throw new IllegalStateException("No expression service factory provider available. Did you forget to add the expression-impl dependency?");
    }
}
