/*
 * Copyright 2018 - 2023 Blazebit.
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

package com.blazebit.notify.server.config;

import com.blazebit.domain.declarative.DeclarativeDomainConfiguration;
import com.blazebit.domain.runtime.model.DomainModel;
import com.blazebit.expression.ExpressionServiceFactory;
import com.blazebit.expression.Expressions;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
@ApplicationScoped
public class BlazeDomainProducer {

    @Inject
    EntityManagerFactory entityManagerFactory;

    @Produces
    @ApplicationScoped
    DomainModel createDomainModel(DeclarativeDomainConfiguration configuration) {
        return configuration.createDomainModel();
    }

    @Produces
    @ApplicationScoped
    ExpressionServiceFactory createExpressionServiceFactory(DomainModel domainModel) {
        return Expressions.getDefaultProvider()
                .create(domainModel);
    }
}
