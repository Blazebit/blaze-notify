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
public interface EntityDomainTypeBuilder {

    public EntityDomainTypeBuilder addAttribute(String attributeName, String typeName);

    public EntityDomainTypeBuilder addAttribute(String attributeName, String typeName, MetadataDefinition<?>... metadataDefinitions);

    public EntityDomainTypeBuilder addAttribute(String attributeName, Class<?> javaType);

    public EntityDomainTypeBuilder addAttribute(String attributeName, Class<?> javaType, MetadataDefinition<?>... metadataDefinitions);

    public EntityDomainTypeBuilder addCollectionAttribute(String attributeName, String elementTypeName);

    public EntityDomainTypeBuilder addCollectionAttribute(String attributeName, String elementTypeName, MetadataDefinition<?>... metadataDefinitions);

    public EntityDomainTypeBuilder addCollectionAttribute(String attributeName, Class<?> elementJavaType);

    public EntityDomainTypeBuilder addCollectionAttribute(String attributeName, Class<?> elementJavaType, MetadataDefinition<?>... metadataDefinitions);

    public EntityDomainTypeBuilder withMetadata(MetadataDefinition<?> metadataDefinition);

    public DomainBuilder build();

}
