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

package com.blazebit.notify.domain.declarative.impl.spi;

import com.blazebit.notify.domain.Domain;
import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.declarative.DeclarativeDomainConfiguration;
import com.blazebit.notify.domain.declarative.spi.DeclarativeDomainBuilderProvider;
import com.blazebit.notify.domain.declarative.spi.DeclarativeMetadataProcessor;
import com.blazebit.notify.domain.spi.DomainContributor;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DeclarativeDomainBuilderProviderImpl implements DeclarativeDomainBuilderProvider {

    @Override
    public DeclarativeDomainConfiguration createEmptyBuilder() {
        return new DeclarativeDomainConfigurationImpl(Domain.getDefaultProvider().createEmptyBuilder());
    }

    @Override
    public DeclarativeDomainConfiguration createDefaultConfiguration() {
        DomainBuilder domainBuilder = Domain.getDefaultProvider().createDefaultBuilder();
        for (DomainContributor domainContributor : ServiceLoader.load(DomainContributor.class)) {
            domainContributor.contribute(domainBuilder);
        }
        DeclarativeDomainConfigurationImpl domainConfiguration = new DeclarativeDomainConfigurationImpl(domainBuilder);
        for (DeclarativeMetadataProcessor<Annotation> processor : ServiceLoader.load(DeclarativeMetadataProcessor.class)) {
            domainConfiguration.withMetadataProcessor(processor);
        }

        return domainConfiguration;
    }
}
