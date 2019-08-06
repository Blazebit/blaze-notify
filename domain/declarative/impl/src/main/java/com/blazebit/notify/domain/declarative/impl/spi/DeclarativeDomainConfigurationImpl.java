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

package com.blazebit.notify.domain.declarative.impl.spi;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.boot.model.EntityDomainTypeBuilder;
import com.blazebit.notify.domain.boot.model.MetadataDefinition;
import com.blazebit.notify.domain.declarative.*;
import com.blazebit.notify.domain.declarative.spi.DeclarativeMetadataProcessor;
import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DeclarativeDomainConfigurationImpl implements DeclarativeDomainConfiguration {

    private static final Logger LOG = Logger.getLogger(DeclarativeDomainConfigurationImpl.class.getName());
    private static final MetadataDefinition[] EMPTY = new MetadataDefinition[0];
    private final DomainBuilder domainBuilder;
    private final Map<Class<? extends Annotation>, DeclarativeMetadataProcessor<Annotation>> metadataProcessors = new HashMap<>();

    public DeclarativeDomainConfigurationImpl(DomainBuilder domainBuilder) {
        this.domainBuilder = domainBuilder;
    }

    @Override
    public DeclarativeDomainConfiguration withMetadataProcessor(DeclarativeMetadataProcessor<? extends Annotation> metadataProcessor) {
        metadataProcessors.put(metadataProcessor.getProcessingAnnotation(), (DeclarativeMetadataProcessor<Annotation>) metadataProcessor);
        return this;
    }

    @Override
    public DomainModel createDomainModel() {
        return domainBuilder.build();
    }

    @Override
    public DeclarativeDomainConfiguration addDomainFunctions(Class<?> domainFunctionsClass) {
        return null;
    }

    @Override
    public DeclarativeDomainConfiguration addDomainType(Class<?> domainTypeClass) {
        DomainType domainType = AnnotationUtils.findAnnotation(domainTypeClass, DomainType.class);
        if (domainType == null) {
            throw new IllegalArgumentException("No domain type annotation found on type: " + domainTypeClass);
        }

        String name = domainType.value();
        if (name.isEmpty()) {
            name = domainTypeClass.getSimpleName();
        }

        boolean implicitDiscovery = domainType.discoverMode() != DiscoverMode.EXPLICIT;

        // automatic metadata discovery via meta annotations
        Set<Class<?>> superTypes = ReflectionUtils.getSuperTypes(domainTypeClass);
        List<MetadataDefinition<?>> metadataDefinitions = new ArrayList<>();

        for (Class<?> type : superTypes) {
            for (Annotation a : type.getAnnotations()) {
                if (a.annotationType().getAnnotation(Metadata.class) != null) {
                    DeclarativeMetadataProcessor<Annotation> processor = metadataProcessors.get(a.annotationType());
                    if (processor == null) {
                        LOG.warning("No processor for metadata annotation type registered: " + a.annotationType());
                    } else {
                        metadataDefinitions.add(processor.process(a));
                    }
                }
            }
        }

        if (domainTypeClass.isEnum()) {
            throw new UnsupportedOperationException("Enum type is not yet supported: " + domainTypeClass);
        } else {
            EntityDomainTypeBuilder entityType = domainBuilder.createEntityType(name, domainTypeClass);
            for (int i = 0; i < metadataDefinitions.size(); i++) {
                entityType.withMetadata(metadataDefinitions.get(i));
            }

            Set<String> handledMethods = new HashSet<>();

            for (Class<?> c : superTypes) {
                for (Method method : c.getDeclaredMethods()) {
                    if (!Modifier.isPrivate(method.getModifiers()) && !method.isBridge()) {
                        final String methodName = method.getName();
                        if (handledMethods.add(methodName)) {
                            handleMethod(entityType, domainTypeClass, method, implicitDiscovery);
                        }
                    }
                }
            }

            entityType.build();
        }

        return this;
    }

    private void handleMethod(EntityDomainTypeBuilder entityType, Class<?> domainTypeClass, Method method, boolean implicitDiscovery) {
        DomainAttribute domainAttribute = AnnotationUtils.findAnnotation(method, DomainAttribute.class);
        if (domainAttribute == null) {
            if (implicitDiscovery && ReflectionUtils.isGetter(method) && AnnotationUtils.findAnnotation(method, Transient.class) == null) {
                domainAttribute = new DomainAttributeLiteral();
            } else {
                return;
            }
        } else if (!ReflectionUtils.isGetter(method)) {
            throw new IllegalArgumentException("Non-getter can't be a domain type attribute: " + method);
        }

        String name = getAttributeName(method);
        Class<?> type = domainAttribute.value();
        String typeName = domainAttribute.typeName();

        // automatic metadata discovery via meta annotations
        List<MetadataDefinition<?>> metadataDefinitions = new ArrayList<>();
        for (Annotation a : AnnotationUtils.getAllAnnotations(method)) {
            if (a.annotationType().getAnnotation(Metadata.class) != null) {
                DeclarativeMetadataProcessor<Annotation> processor = metadataProcessors.get(a.annotationType());
                if (processor == null) {
                    LOG.warning("No processor for metadata annotation type registered: " + a.annotationType());
                } else {
                    metadataDefinitions.add(processor.process(a));
                }
            }
        }

        MetadataDefinition[] metadataDefinitionArray;
        if (metadataDefinitions.isEmpty()) {
            metadataDefinitionArray = EMPTY;
        } else {
            metadataDefinitionArray = metadataDefinitions.toArray(new MetadataDefinition[metadataDefinitions.size()]);
        }

        if (!typeName.isEmpty()) {
            entityType.addAttribute(name, typeName, metadataDefinitionArray);
        } else {
            Class<?> returnType = type == void.class ? ReflectionUtils.getResolvedMethodReturnType(domainTypeClass, method) : type;
            // TODO: type resolver SPI to resolve types based on java types?
            if (Collection.class.isAssignableFrom(returnType)) {
                Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(domainTypeClass, method);
                entityType.addCollectionAttribute(name, typeArguments[0], metadataDefinitionArray);
            } else {
                entityType.addAttribute(name, returnType, metadataDefinitionArray);
            }
        }
    }

    protected static String getAttributeName(Method getterOrSetter) {
        String name = getterOrSetter.getName();
        StringBuilder sb = new StringBuilder(name.length());
        int index = name.startsWith("is") ? 2 : 3;
        char firstAttributeNameChar = name.charAt(index);
        return sb.append(Character.toLowerCase(firstAttributeNameChar))
                .append(name, index + 1, name.length())
                .toString();
    }
}
