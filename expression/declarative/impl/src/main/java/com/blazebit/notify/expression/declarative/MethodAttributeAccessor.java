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

package com.blazebit.notify.expression.declarative;

import com.blazebit.notify.domain.boot.model.MetadataDefinition;
import com.blazebit.notify.domain.boot.model.MetadataDefinitionHolder;
import com.blazebit.notify.domain.runtime.model.EntityDomainTypeAttribute;
import com.blazebit.notify.expression.spi.AttributeAccessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodAttributeAccessor implements MetadataDefinition<AttributeAccessor>, AttributeAccessor {

    private final Method getter;

    public MethodAttributeAccessor(Method getter) {
        this.getter = getter;
    }

    @Override
    public Object getAttribute(Object value, EntityDomainTypeAttribute attribute) {
        try {
            return getter.invoke(value);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't access attribute " + attribute + " on object: " + value, e);
        }
    }

    @Override
    public Class<AttributeAccessor> getJavaType() {
        return AttributeAccessor.class;
    }

    @Override
    public AttributeAccessor build(MetadataDefinitionHolder<?> definitionHolder) {
        return this;
    }
}