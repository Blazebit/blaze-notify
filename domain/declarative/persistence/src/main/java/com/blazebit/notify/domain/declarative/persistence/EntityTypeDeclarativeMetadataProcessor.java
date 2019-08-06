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

public class EntityTypeDeclarativeMetadataProcessor implements DeclarativeMetadataProcessor<EntityType> {

    @Override
    public Class<EntityType> getProcessingAnnotation() {
        return EntityType.class;
    }

    @Override
    public MetadataDefinition<?> process(EntityType annotation) {
        return new EntityTypeImpl(annotation);
    }

    private static class EntityTypeImpl implements com.blazebit.notify.domain.persistence.EntityType, MetadataDefinition<com.blazebit.notify.domain.persistence.EntityType> {

        private final Class<?> entityClass;
        private final String entityName;

        public EntityTypeImpl(EntityType entityType) {
            this(entityType.value(), getEntityName(entityType.value(), entityType.entityName()));
        }

        private static String getEntityName(Class<?> entityClass, String entityName) {
            if (!entityName.isEmpty()) {
                return entityName;
            }

            return entityClass.getName();
        }

        public EntityTypeImpl(Class<?> entityClass, String entityName) {
            this.entityClass = entityClass;
            this.entityName = entityName;
        }

        @Override
        public Class<?> getEntityClass() {
            return entityClass;
        }

        @Override
        public String getEntityName() {
            return entityName;
        }

        @Override
        public Class<com.blazebit.notify.domain.persistence.EntityType> getJavaType() {
            return com.blazebit.notify.domain.persistence.EntityType.class;
        }

        @Override
        public com.blazebit.notify.domain.persistence.EntityType build(MetadataDefinitionHolder<?> definitionHolder) {
            return this;
        }
    }
}
