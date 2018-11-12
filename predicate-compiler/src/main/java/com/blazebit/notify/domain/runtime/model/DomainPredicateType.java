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

package com.blazebit.notify.domain.runtime.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public enum DomainPredicateType {
    NULLNESS,
    COLLECTION,
    RELATIONAL,
    EQUALITY;

    public static Set<DomainPredicateType> comparable() {
        return EnumSet.of(DomainPredicateType.RELATIONAL, DomainPredicateType.EQUALITY, DomainPredicateType.NULLNESS);
    }

    public static Set<DomainPredicateType> equality() {
        return EnumSet.of(DomainPredicateType.EQUALITY, DomainPredicateType.NULLNESS);
    }
}
