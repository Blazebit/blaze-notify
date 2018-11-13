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

package com.blazebit.notify.domain.impl.boot.model;

import com.blazebit.notify.domain.boot.model.EnumDomainTypeBuilder;
import com.blazebit.notify.domain.boot.model.MetadataDefinition;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EnumDomainTypeBuilderImpl implements EnumDomainTypeBuilder {

    private final DomainBuilderImpl domainBuilder;
    private final EnumDomainTypeDefinitionImpl domainTypeDefinition;

    public EnumDomainTypeBuilderImpl(DomainBuilderImpl domainBuilder, String name, Class<? extends Enum<?>> javaType) {
        this.domainBuilder = domainBuilder;
        this.domainTypeDefinition = new EnumDomainTypeDefinitionImpl(name, javaType);
    }

    @Override
    public EnumDomainTypeBuilder withValue(String value) {
        domainTypeDefinition.addEnumValue(new EnumDomainTypeValueDefinitionImpl(domainTypeDefinition, value));
        return this;
    }

    @Override
    public EnumDomainTypeBuilder withValue(String value, MetadataDefinition<?>... metadataDefinitions) {
        EnumDomainTypeValueDefinitionImpl valueDefinition = new EnumDomainTypeValueDefinitionImpl(domainTypeDefinition, value);
        for (MetadataDefinition<?> metadataDefinition : metadataDefinitions) {
            valueDefinition.withMetadataDefinition(metadataDefinition);
        }

        domainTypeDefinition.addEnumValue(valueDefinition);
        return this;
    }

    @Override
    public EnumDomainTypeBuilder withMetadata(MetadataDefinition<?> metadataDefinition) {
        domainTypeDefinition.withMetadataDefinition(metadataDefinition);
        return this;
    }

    @Override
    public DomainBuilderImpl build() {
        return domainBuilder.withDomainTypeDefinition(domainTypeDefinition);
    }
}
