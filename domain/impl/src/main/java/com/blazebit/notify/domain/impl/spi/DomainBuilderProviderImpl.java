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

package com.blazebit.notify.domain.impl.spi;

import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.impl.boot.model.DomainBuilderImpl;
import com.blazebit.notify.domain.impl.runtime.model.basic.JavaDomainModel;
import com.blazebit.notify.domain.spi.DomainBuilderProvider;

import java.util.Calendar;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DomainBuilderProviderImpl implements DomainBuilderProvider {

    @Override
    public DomainBuilder createEmptyBuilder() {
        return new DomainBuilderImpl();
    }

    @Override
    public DomainBuilder createDefaultBuilder() {
        DomainBuilderImpl domainBuilder = new DomainBuilderImpl();
        domainBuilder.withDomainType(JavaDomainModel.INTEGER);
        domainBuilder.withDomainType(JavaDomainModel.DECIMAL);
        domainBuilder.withDomainType(JavaDomainModel.BOOLEAN);
        domainBuilder.withDomainType(JavaDomainModel.CALENDAR);
        domainBuilder.withDomainType(JavaDomainModel.STRING);
        domainBuilder.withLiteralTypeResolver(JavaDomainModel.NUMERIC_LITERAL_TYPE_RESOLVER);
        domainBuilder.withLiteralTypeResolver(JavaDomainModel.STRING_LITERAL_TYPE_RESOLVER);
        domainBuilder.withLiteralTypeResolver(JavaDomainModel.TEMPORAL_LITERAL_TYPE_RESOLVER);
        domainBuilder.withLiteralTypeResolver(JavaDomainModel.BOOLEAN_LITERAL_TYPE_RESOLVER);
        domainBuilder.createFunction("CURRENT_TIMESTAMP")
                .withExactArgumentCount(0)
                .withResultType(Calendar.class)
                .build();
        return domainBuilder;
    }
}
