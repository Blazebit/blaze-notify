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

package com.blazebit.notify.domain.impl.runtime.model.basic;

import com.blazebit.notify.domain.impl.runtime.model.BasicDomainTypeImpl;
import com.blazebit.notify.domain.runtime.model.BasicDomainType;
import com.blazebit.notify.domain.runtime.model.DomainOperator;
import com.blazebit.notify.domain.runtime.model.DomainPredicateType;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.EnumSet;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class BasicDomainTypes {

    public static final BasicDomainType INTEGER = new BasicDomainTypeImpl(Integer.class, DomainOperator.arithmetic(), DomainPredicateType.comparable());
    public static final BasicDomainType DECIMAL = new BasicDomainTypeImpl(BigDecimal.class, DomainOperator.arithmetic(), DomainPredicateType.comparable());
    public static final BasicDomainType STRING = new BasicDomainTypeImpl(String.class, DomainOperator.arithmetic(), DomainPredicateType.comparable());
    public static final BasicDomainType CALENDAR = new BasicDomainTypeImpl(Calendar.class, EnumSet.of(DomainOperator.PLUS, DomainOperator.MINUS), DomainPredicateType.comparable());
    public static final BasicDomainType BOOLEAN = new BasicDomainTypeImpl(Boolean.class, EnumSet.of(DomainOperator.NOT), DomainPredicateType.equality());

}
