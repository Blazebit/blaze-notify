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
import com.blazebit.notify.domain.declarative.spi.DeclarativeAttributeMetadataProcessor;
import com.blazebit.notify.domain.declarative.spi.DeclarativeMetadataProcessor;

import java.lang.reflect.Method;

public class EntityAttributeDeclarativeMetadataProcessor implements DeclarativeAttributeMetadataProcessor<EntityAttribute> {

    @Override
    public Class<EntityAttribute> getProcessingAnnotation() {
        return EntityAttribute.class;
    }

    @Override
    public MetadataDefinition<?> process(Class<?> annotatedClass, Method method, EntityAttribute annotation) {
        return new EntityAttributeImpl(annotation);
    }

}
