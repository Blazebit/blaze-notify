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

import com.blazebit.notify.domain.runtime.model.*;

import java.util.*;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class StaticDomainFunctionTypeResolvers {

    private static final Map<String, DomainFunctionTypeResolver> RETURNING_TYPE_NAME_CACHE = new HashMap<>();
    private static final Map<Class<?>, DomainFunctionTypeResolver> RETURNING_JAVA_TYPE_CACHE = new HashMap<>();
    private static final Map<ClassArray, DomainFunctionTypeResolver> WIDEST_CACHE = new HashMap<>();

    public static final DomainFunctionTypeResolver FIRST_ARGUMENT_TYPE = new DomainFunctionTypeResolver() {
        @Override
        public DomainType resolveType(DomainModel domainModel, DomainFunction function, Map<DomainFunctionArgument, DomainType> argumentTypes) {
            for (DomainFunctionArgument argument : function.getArguments()) {
                DomainType domainType = argumentTypes.get(argument);
                if (domainType != null) {
                    return domainType;
                }
            }

            return null;
        }
    };

    public static DomainFunctionTypeResolver returning(final String typeName) {
        DomainFunctionTypeResolver domainFunctionTypeResolver = RETURNING_TYPE_NAME_CACHE.get(typeName);
        if (domainFunctionTypeResolver == null) {
            domainFunctionTypeResolver = new DomainFunctionTypeResolver() {
                @Override
                public DomainType resolveType(DomainModel domainModel, DomainFunction function, Map<DomainFunctionArgument, DomainType> argumentTypes) {
                    return domainModel.getType(typeName);
                }
            };
            RETURNING_TYPE_NAME_CACHE.put(typeName, domainFunctionTypeResolver);
        }
        return domainFunctionTypeResolver;
    }

    public static DomainFunctionTypeResolver returning(final Class<?> javaType) {
        DomainFunctionTypeResolver domainFunctionTypeResolver = RETURNING_JAVA_TYPE_CACHE.get(javaType);
        if (domainFunctionTypeResolver == null) {
            domainFunctionTypeResolver = new DomainFunctionTypeResolver() {
                @Override
                public DomainType resolveType(DomainModel domainModel, DomainFunction function, Map<DomainFunctionArgument, DomainType> argumentTypes) {
                    return domainModel.getType(javaType);
                }
            };
            RETURNING_JAVA_TYPE_CACHE.put(javaType, domainFunctionTypeResolver);
        }
        return domainFunctionTypeResolver;
    }

    public static DomainFunctionTypeResolver widest(final Class<?>... javaTypes) {
        ClassArray key = new ClassArray(javaTypes);
        DomainFunctionTypeResolver domainFunctionTypeResolver = WIDEST_CACHE.get(key);
        if (domainFunctionTypeResolver == null) {
            domainFunctionTypeResolver = new DomainFunctionTypeResolver() {
                @Override
                public DomainType resolveType(DomainModel domainModel, DomainFunction function, Map<DomainFunctionArgument, DomainType> argumentTypes) {
                    List<DomainType> preferredTypes = new ArrayList<>(javaTypes.length);
                    for (Class<?> javaType : javaTypes) {
                        preferredTypes.add(domainModel.getType(javaType));
                    }
                    Collection<DomainType> domainTypes = argumentTypes.values();
                    for (DomainType preferredType : preferredTypes) {
                        if (domainTypes.contains(preferredType)) {
                            return preferredType;
                        }
                    }

                    return argumentTypes.isEmpty() ? preferredTypes.get(0) : domainTypes.iterator().next();
                }
            };
            WIDEST_CACHE.put(key, domainFunctionTypeResolver);
        }
        return domainFunctionTypeResolver;
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
