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

package com.blazebit.notify.domain.boot.model;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface DomainFunctionBuilder {

    public DomainFunctionBuilder withMinArgumentCount(int minArgumentCount);

    public DomainFunctionBuilder withExactArgumentCount(int exactArgumentCount);

    public DomainFunctionBuilder withArgument(String name);

    public DomainFunctionBuilder withArgument(String name, MetadataDefinition<?>... metadataDefinitions);

    public DomainFunctionBuilder withArgument(String name, String typeName);

    public DomainFunctionBuilder withArgument(String name, String typeName, MetadataDefinition<?>... metadataDefinitions);

    public DomainFunctionBuilder withArgument(String name, Class<?> javaType);

    public DomainFunctionBuilder withArgument(String name, Class<?> javaType, MetadataDefinition<?>... metadataDefinitions);

    public DomainFunctionBuilder withCollectionArgument(String name);

    public DomainFunctionBuilder withCollectionArgument(String name, MetadataDefinition<?>... metadataDefinitions);

    public DomainFunctionBuilder withCollectionArgument(String name, String typeName);

    public DomainFunctionBuilder withCollectionArgument(String name, String typeName, MetadataDefinition<?>... metadataDefinitions);

    public DomainFunctionBuilder withCollectionArgument(String name, Class<?> javaType);

    public DomainFunctionBuilder withCollectionArgument(String name, Class<?> javaType, MetadataDefinition<?>... metadataDefinitions);

    public DomainFunctionBuilder withArgumentTypes(String... typeNames);

    public DomainFunctionBuilder withArgumentTypes(Class<?>... typeNames);

    public DomainFunctionBuilder withResultType(String typeName);

    public DomainFunctionBuilder withResultType(Class<?> javaType);

    public DomainFunctionBuilder withCollectionResultType();

    public DomainFunctionBuilder withCollectionResultType(String typeName);

    public DomainFunctionBuilder withCollectionResultType(Class<?> javaType);

    public DomainFunctionBuilder withMetadata(MetadataDefinition<?> metadataDefinition);

    public DomainBuilder build();

}
