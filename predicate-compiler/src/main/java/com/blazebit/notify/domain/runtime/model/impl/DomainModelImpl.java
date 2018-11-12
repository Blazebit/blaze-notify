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

package com.blazebit.notify.domain.runtime.model.impl;

import com.blazebit.notify.domain.runtime.model.DomainFunction;
import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.domain.runtime.model.DomainType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DomainModelImpl implements DomainModel {

    private final Map<String, DomainType> domainTypes;
    private final Map<Class<?>, DomainType> domainTypesByJavaType;
    private final Map<String, DomainFunction> domainFunctions;

    public DomainModelImpl(Map<String, DomainType> domainTypes, Map<String, DomainFunction> domainFunctions) {
        this.domainTypes = domainTypes;
        this.domainFunctions = domainFunctions;
        Map<Class<?>, DomainType> domainTypesByJavaType = new HashMap<>(domainTypes.size());
        for (DomainType domainType : domainTypes.values()) {
            if (domainType.getJavaType() != null) {
                domainTypesByJavaType.put(domainType.getJavaType(), domainType);
            }
        }

        this.domainTypesByJavaType = domainTypesByJavaType;
    }

    @Override
    public DomainType getType(String name) {
        return domainTypes.get(name);
    }

    @Override
    public DomainType getType(Class<?> javaType) {
        return domainTypesByJavaType.get(javaType);
    }

    @Override
    public Map<String, DomainType> getTypes() {
        return domainTypes;
    }

    @Override
    public DomainFunction getFunction(String name) {
        return domainFunctions.get(name);
    }
}
