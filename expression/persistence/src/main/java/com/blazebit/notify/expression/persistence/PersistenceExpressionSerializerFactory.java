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

package com.blazebit.notify.expression.persistence;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.expression.Expression;
import com.blazebit.notify.expression.ExpressionSerializer;
import com.blazebit.notify.expression.spi.ExpressionSerializerFactory;
import com.blazebit.persistence.WhereBuilder;

import java.util.Map;

@ServiceProvider(ExpressionSerializerFactory.class)
public class PersistenceExpressionSerializerFactory implements ExpressionSerializerFactory<WhereBuilder<?>> {

    @Override
    public Class<WhereBuilder<?>> getSerializationTargetType() {
        return (Class<WhereBuilder<?>>) (Class) WhereBuilder.class;
    }

    @Override
    public ExpressionSerializer<WhereBuilder<?>> createSerializer(DomainModel domainModel) {
        return new PersistenceExpressionSerializer(domainModel);
    }
}
