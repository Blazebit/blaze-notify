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

package com.blazebit.notify.domain.boot.model;

import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.domain.runtime.model.DomainOperator;
import com.blazebit.notify.domain.runtime.model.DomainPredicateType;
import com.blazebit.notify.domain.runtime.model.DomainType;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface DomainBuilder {

    public DomainBuilder withDomainType(DomainType entityDomainType);

    public DomainBuilder withOperator(String typeName, DomainOperator operator);

    public DomainBuilder withOperator(String typeName, DomainOperator... operators);

    public DomainBuilder withPredicate(String typeName, DomainPredicateType predicate);

    public DomainBuilder withPredicate(String typeName, DomainPredicateType... predicates);

    public DomainFunctionBuilder createFunction(String name);

    public EntityDomainTypeBuilder createEntityType(String name);

    public EntityDomainTypeBuilder createEntityType(String name, Class<?> javaType);

    public DomainModel build();
}