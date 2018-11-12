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

package com.blazebit.notify.domain.runtime.model;

import com.blazebit.notify.domain.Domain;
import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.boot.model.MetadataDefinition;
import com.blazebit.notify.domain.boot.model.MetadataDefinitionHolder;
import org.junit.Assert;
import org.junit.Test;

public class DomainBuilderTest {

    @Test
    public void testBuildSimpleModel() {
        // Given
        DomainBuilder domainBuilder = Domain.getDefaultProvider().createDefaultBuilder();
        domainBuilder.createEntityType("Test")
                .addAttribute("name", "String", MetadataSample.INSTANCE)
                .withMetadata(MetadataSample.INSTANCE)
        .build();

        // When
        DomainModel domainModel = domainBuilder.build();

        // Then
        EntityDomainType entityDomainType = (EntityDomainType) domainModel.getType("Test");
        Assert.assertEquals(MetadataSample.INSTANCE, entityDomainType.getMetadata(MetadataSample.class));
        Assert.assertEquals(MetadataSample.INSTANCE, entityDomainType.getAttribute("name").getMetadata(MetadataSample.class));
        Assert.assertEquals("String", entityDomainType.getAttribute("name").getType().getName());
    }

    @Test
    public void testBuildCollectionModel() {
        // Given
        DomainBuilder domainBuilder = Domain.getDefaultProvider().createDefaultBuilder();
        domainBuilder.createEntityType("Test")
                .addCollectionAttribute("names", "String", MetadataSample.INSTANCE)
                .withMetadata(MetadataSample.INSTANCE)
                .build();

        // When
        DomainModel domainModel = domainBuilder.build();

        // Then
        EntityDomainType entityDomainType = (EntityDomainType) domainModel.getType("Test");
        Assert.assertEquals(MetadataSample.INSTANCE, entityDomainType.getMetadata(MetadataSample.class));
        Assert.assertEquals(MetadataSample.INSTANCE, entityDomainType.getAttribute("names").getMetadata(MetadataSample.class));
        Assert.assertEquals("Collection", entityDomainType.getAttribute("names").getType().getName());
        Assert.assertEquals("String", ((CollectionDomainType) entityDomainType.getAttribute("names").getType()).getElementType().getName());
    }

    @Test
    public void testBuildEnumModel() {
        // Given
        DomainBuilder domainBuilder = Domain.getDefaultProvider().createDefaultBuilder();
        domainBuilder.createEnumType("TestKind")
                .withValue("UnitTest", MetadataSample.INSTANCE)
                .withValue("IntegrationTest", MetadataSample.INSTANCE)
                .withMetadata(MetadataSample.INSTANCE)
                .build();
        domainBuilder.createEntityType("Test")
                .addAttribute("kind", "TestKind", MetadataSample.INSTANCE)
                .withMetadata(MetadataSample.INSTANCE)
                .build();

        // When
        DomainModel domainModel = domainBuilder.build();

        // Then
        EntityDomainType entityDomainType = (EntityDomainType) domainModel.getType("Test");
        EnumDomainType enumDomainType = (EnumDomainType) domainModel.getType("TestKind");
        Assert.assertEquals(MetadataSample.INSTANCE, entityDomainType.getMetadata(MetadataSample.class));
        Assert.assertEquals(MetadataSample.INSTANCE, entityDomainType.getAttribute("kind").getType().getMetadata(MetadataSample.class));
        Assert.assertEquals(MetadataSample.INSTANCE, enumDomainType.getEnumValues().get("UnitTest").getMetadata(MetadataSample.class));
        Assert.assertEquals("TestKind", entityDomainType.getAttribute("kind").getType().getName());
        Assert.assertEquals(2, ((EnumDomainType) entityDomainType.getAttribute("kind").getType()).getEnumValues().size());
    }

    private static class MetadataSample implements MetadataDefinition<MetadataSample> {

        public static final MetadataSample INSTANCE = new MetadataSample();

        @Override
        public Class<MetadataSample> getJavaType() {
            return MetadataSample.class;
        }

        @Override
        public MetadataSample build(MetadataDefinitionHolder<?> definitionHolder) {
            return this;
        }
    }
}
