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
import org.junit.Assert;
import org.junit.Test;

public class DomainBuilderTest {

    @Test
    public void testBuildSimpleModel() {
        // Given
        DomainBuilder domainBuilder = Domain.getDefaultProvider().createDefaultBuilder();
        domainBuilder.createEntityType("Test")
                .addAttribute("name", "String")
        .build();

        // When
        DomainModel domainModel = domainBuilder.build();

        // Then
        EntityDomainType entityDomainType = (EntityDomainType) domainModel.getType("Test");
        Assert.assertEquals("String", entityDomainType.getAttribute("name").getType().getName());
    }

    @Test
    public void testBuildCollectionModel() {
        // Given
        DomainBuilder domainBuilder = Domain.getDefaultProvider().createDefaultBuilder();
        domainBuilder.createEntityType("Test")
                .addCollectionAttribute("names", "String")
                .build();

        // When
        DomainModel domainModel = domainBuilder.build();

        // Then
        EntityDomainType entityDomainType = (EntityDomainType) domainModel.getType("Test");
        Assert.assertEquals("Collection", entityDomainType.getAttribute("names").getType().getName());
        Assert.assertEquals("String", ((CollectionDomainType) entityDomainType.getAttribute("names").getType()).getElementType().getName());
    }
}
