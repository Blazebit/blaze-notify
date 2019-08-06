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

package com.blazebit.notify.server.config;

import com.blazebit.notify.domain.declarative.DeclarativeDomainConfiguration;
import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.expression.ExpressionServiceFactory;
import com.blazebit.notify.expression.Expressions;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

@ApplicationScoped
public class BlazeDomainProducer {

    @Inject
    EntityManagerFactory entityManagerFactory;

    @Produces
    @ApplicationScoped
    DomainModel createDomainModel(DeclarativeDomainConfiguration configuration) {
        return configuration.createDomainModel();
//        return Domain.getDefaultProvider()
//                .createDefaultBuilder()
//                    .createEntityType("EmailNotificationRecipient", EmailNotificationRecipient.class)
//                        // TODO: currently this must be a BigDecimal to be able to support the equality predicate with a literal?!
//                        .addAttribute("id", BigDecimal.class)
//                    .build()
//                .build();
    }

    @Produces
    @ApplicationScoped
    ExpressionServiceFactory createExpressionServiceFactory(DomainModel domainModel) {
        return Expressions.getDefaultProvider()
                .create(domainModel);
    }
}
