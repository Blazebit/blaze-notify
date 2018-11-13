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

package com.blazebit.notify.domain.persistence;

import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.domain.runtime.model.DomainOperationTypeResolver;
import com.blazebit.notify.domain.runtime.model.DomainType;

import java.util.*;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class StaticDomainOperationTypeResolvers {

    private static final Map<String, DomainOperationTypeResolver> RETURNING_TYPE_NAME_CACHE = new HashMap<>();
    private static final Map<Class<?>, DomainOperationTypeResolver> RETURNING_JAVA_TYPE_CACHE = new HashMap<>();
    private static final Map<ClassArray, DomainOperationTypeResolver> WIDEST_CACHE = new HashMap<>();

    public static DomainOperationTypeResolver returning(final String typeName) {
        DomainOperationTypeResolver domainOperationTypeResolver = RETURNING_TYPE_NAME_CACHE.get(typeName);
        if (domainOperationTypeResolver == null) {
            domainOperationTypeResolver = new DomainOperationTypeResolver() {
                @Override
                public DomainType resolveType(DomainModel domainModel, List<DomainType> domainTypes) {
                    return domainModel.getType(typeName);
                }
            };
            RETURNING_TYPE_NAME_CACHE.put(typeName, domainOperationTypeResolver);
        }
        return domainOperationTypeResolver;
    }

    public static DomainOperationTypeResolver returning(final Class<?> javaType) {
        DomainOperationTypeResolver domainOperationTypeResolver = RETURNING_JAVA_TYPE_CACHE.get(javaType);
        if (domainOperationTypeResolver == null) {
            domainOperationTypeResolver = new DomainOperationTypeResolver() {
                @Override
                public DomainType resolveType(DomainModel domainModel, List<DomainType> domainTypes) {
                    return domainModel.getType(javaType);
                }
            };
            RETURNING_JAVA_TYPE_CACHE.put(javaType, domainOperationTypeResolver);
        }
        return domainOperationTypeResolver;
    }

    public static DomainOperationTypeResolver widest(final Class<?>... javaTypes) {
        ClassArray key = new ClassArray(javaTypes);
        DomainOperationTypeResolver domainOperationTypeResolver = WIDEST_CACHE.get(key);
        if (domainOperationTypeResolver == null) {
            domainOperationTypeResolver = new DomainOperationTypeResolver() {
                @Override
                public DomainType resolveType(DomainModel domainModel, List<DomainType> domainTypes) {
                    List<DomainType> preferredTypes = new ArrayList<>(javaTypes.length);
                    for (Class<?> javaType : javaTypes) {
                        preferredTypes.add(domainModel.getType(javaType));
                    }
                    for (DomainType preferredType : preferredTypes) {
                        if (domainTypes.contains(preferredType)) {
                            return preferredType;
                        }
                    }

                    return domainTypes.isEmpty() ? preferredTypes.get(0) : domainTypes.get(0);
                }
            };
            WIDEST_CACHE.put(key, domainOperationTypeResolver);
        }
        return domainOperationTypeResolver;
    }

    private static class ClassArray {

        private final Class<?>[] classes;

        public ClassArray(Class<?>[] classes) {
            this.classes = classes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            ClassArray that = (ClassArray) o;
            return Arrays.equals(classes, that.classes);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(classes);
        }
    }
}
