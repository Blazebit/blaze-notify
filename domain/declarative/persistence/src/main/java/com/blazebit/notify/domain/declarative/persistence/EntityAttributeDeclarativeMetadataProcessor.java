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

package com.blazebit.notify.domain.declarative.persistence;

import com.blazebit.notify.domain.boot.model.MetadataDefinition;
import com.blazebit.notify.domain.boot.model.MetadataDefinitionHolder;
import com.blazebit.notify.domain.declarative.spi.DeclarativeMetadataProcessor;

public class EntityAttributeDeclarativeMetadataProcessor implements DeclarativeMetadataProcessor<EntityAttribute> {

    @Override
    public Class<EntityAttribute> getProcessingAnnotation() {
        return EntityAttribute.class;
    }

    @Override
    public MetadataDefinition<?> process(EntityAttribute annotation) {
        return new EntityTypeImpl(annotation);
    }

    private static class EntityTypeImpl implements com.blazebit.notify.domain.persistence.EntityAttribute, MetadataDefinition<com.blazebit.notify.domain.persistence.EntityAttribute> {

        private final String expression;

        public EntityTypeImpl(EntityAttribute entityAttribute) {
            this(entityAttribute.value());
        }

        public EntityTypeImpl(String expression) {
            this.expression = expression;
        }

        @Override
        public String getExpression() {
            return expression;
        }

        @Override
        public Class<com.blazebit.notify.domain.persistence.EntityAttribute> getJavaType() {
            return com.blazebit.notify.domain.persistence.EntityAttribute.class;
        }

        @Override
        public com.blazebit.notify.domain.persistence.EntityAttribute build(MetadataDefinitionHolder<?> definitionHolder) {
            return this;
        }
    }
}
