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
import com.blazebit.notify.domain.boot.model.*;
import com.blazebit.notify.domain.declarative.*;
import com.blazebit.notify.domain.declarative.spi.*;
import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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
    private final Map<Class<? extends Annotation>, List<DeclarativeMetadataProcessor<Annotation>>> entityMetadataProcessors = new HashMap<>();
    private final Map<Class<? extends Annotation>, List<DeclarativeAttributeMetadataProcessor<Annotation>>> attributeMetadataProcessors = new HashMap<>();
    private final Map<Class<? extends Annotation>, List<DeclarativeFunctionMetadataProcessor<Annotation>>> functionMetadataProcessors = new HashMap<>();
    private final Map<Class<? extends Annotation>, List<DeclarativeFunctionParameterMetadataProcessor<Annotation>>> functionParameterMetadataProcessors = new HashMap<>();
    private TypeResolver typeResolver;

    public DeclarativeDomainConfigurationImpl(DomainBuilder domainBuilder) {
        this.domainBuilder = domainBuilder;
    }

    @Override
    public DeclarativeDomainConfiguration withMetadataProcessor(DeclarativeMetadataProcessor<? extends Annotation> metadataProcessor) {
        entityMetadataProcessors.compute(metadataProcessor.getProcessingAnnotation(), (k, v) -> new ArrayList<>()).add((DeclarativeMetadataProcessor<Annotation>) metadataProcessor);
        return this;
    }

    @Override
    public DeclarativeDomainConfiguration withMetadataProcessor(DeclarativeAttributeMetadataProcessor<? extends Annotation> metadataProcessor) {
        attributeMetadataProcessors.compute(metadataProcessor.getProcessingAnnotation(), (k, v) -> new ArrayList<>()).add((DeclarativeAttributeMetadataProcessor<Annotation>) metadataProcessor);
        return this;
    }

    @Override
    public DeclarativeDomainConfiguration withMetadataProcessor(DeclarativeFunctionMetadataProcessor<? extends Annotation> metadataProcessor) {
        functionMetadataProcessors.compute(metadataProcessor.getProcessingAnnotation(), (k, v) -> new ArrayList<>()).add((DeclarativeFunctionMetadataProcessor<Annotation>) metadataProcessor);
        return this;
    }

    @Override
    public DeclarativeDomainConfiguration withMetadataProcessor(DeclarativeFunctionParameterMetadataProcessor<? extends Annotation> metadataProcessor) {
        functionParameterMetadataProcessors.compute(metadataProcessor.getProcessingAnnotation(), (k, v) -> new ArrayList<>()).add((DeclarativeFunctionParameterMetadataProcessor<Annotation>) metadataProcessor);
        return this;
    }

    @Override
    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    @Override
    public DeclarativeDomainConfiguration setTypeResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
        return this;
    }

    @Override
    public DomainModel createDomainModel() {
        return domainBuilder.build();
    }

    @Override
    public DeclarativeDomainConfiguration addDomainFunctions(Class<?> domainFunctionsClass) {
        DomainFunctions domainFunctions = AnnotationUtils.findAnnotation(domainFunctionsClass, DomainFunctions.class);
        if (domainFunctions == null) {
            throw new IllegalArgumentException("No domain functions annotation found on type: " + domainFunctionsClass);
        }

        boolean implicitDiscovery = domainFunctions.discoverMode() != DiscoverMode.EXPLICIT;
        Set<Class<?>> superTypes = ReflectionUtils.getSuperTypes(domainFunctionsClass);
        Set<String> handledMethods = new HashSet<>();

        for (Class<?> c : superTypes) {
            for (Method method : c.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers()) && !method.isBridge()) {
                    final String methodName = method.getName();
                    if (handledMethods.add(methodName)) {
                        handleDomainFunctionMethod(domainFunctionsClass, method, implicitDiscovery);
                    }
                }
            }
        }

        return this;
    }

    private void handleDomainFunctionMethod(Class<?> domainFunctionsClass, Method method, boolean implicitDiscovery) {
        DomainFunction domainFunction = AnnotationUtils.findAnnotation(method, DomainFunction.class);
        if (domainFunction == null) {
            if (implicitDiscovery && AnnotationUtils.findAnnotation(method, Transient.class) == null) {
                domainFunction = new DomainFunctionLiteral();
            } else {
                return;
            }
        }

        String name = domainFunction.value();
        if (name.isEmpty()) {
            name = method.getName();
        }

        Class<?> type = domainFunction.collection() ? Collection.class : domainFunction.type();
        String typeName = domainFunction.collection() ? "Collection" : domainFunction.typeName();
        Class<?> elementType = domainFunction.collection() ? domainFunction.type() : void.class;
        String elementTypeName = domainFunction.collection() ? domainFunction.typeName() : "";

        DomainFunctionBuilder function = domainBuilder.createFunction(name);

        ResolvedType resolvedType = resolveType(typeName, elementTypeName, type, elementType, domainFunctionsClass, method, null);
        if (resolvedType.collection) {
            if (resolvedType.typeName.isEmpty()) {
                function.withCollectionResultType(resolvedType.type);
            } else {
                function.withCollectionResultType(resolvedType.typeName);
            }
        } else {
            if (resolvedType.typeName.isEmpty()) {
                function.withCollectionResultType(resolvedType.type);
            } else {
                function.withCollectionResultType(resolvedType.typeName);
            }
        }

        // automatic metadata discovery via meta annotations
        for (Map.Entry<Class<? extends Annotation>, List<DeclarativeFunctionMetadataProcessor<Annotation>>> entry : functionMetadataProcessors.entrySet()) {
            if (entry.getKey() == null) {
                for (DeclarativeFunctionMetadataProcessor<Annotation> processor : entry.getValue()) {
                    MetadataDefinition<?> metadataDefinition = processor.process(domainFunctionsClass, method, null);
                    if (metadataDefinition != null) {
                        function.withMetadata(metadataDefinition);
                    }
                }
            } else {
                Annotation annotation = AnnotationUtils.findAnnotation(method, entry.getKey());
                if (annotation != null) {
                    for (DeclarativeFunctionMetadataProcessor<Annotation> processor : entry.getValue()) {
                        MetadataDefinition<?> metadataDefinition = processor.process(domainFunctionsClass, method, annotation);
                        if (metadataDefinition != null) {
                            function.withMetadata(metadataDefinition);
                        }
                    }
                }
            }
        }

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            handleDomainFunctionParameter(function, domainFunctionsClass, method, parameters[i]);
        }

        function.build();
    }

    private void handleDomainFunctionParameter(DomainFunctionBuilder function, Class<?> domainFunctionsClass, Method method, Parameter parameter) {
        String parameterName = parameter.getName();

        Class<?> type;
        String typeName;
        Class<?> elementType;
        String elementTypeName;
        DomainFunctionParam param = parameter.getAnnotation(DomainFunctionParam.class);
        if (param == null) {
            type = parameter.getType();
            typeName = "";
            elementType = null;
            elementTypeName = "";
        } else {
            if (!param.value().isEmpty()) {
                parameterName = param.value();
            }

            type = param.collection() ? Collection.class : param.type();
            typeName = param.collection() ? "Collection" : param.typeName();
            elementType = param.collection() ? param.type() : void.class;
            elementTypeName = param.collection() ? param.typeName() : "";
        }

        // automatic metadata discovery via meta annotations
        List<MetadataDefinition<?>> metadataDefinitions = new ArrayList<>();
        for (Map.Entry<Class<? extends Annotation>, List<DeclarativeFunctionParameterMetadataProcessor<Annotation>>> entry : functionParameterMetadataProcessors.entrySet()) {
            if (entry.getKey() == null) {
                for (DeclarativeFunctionParameterMetadataProcessor<Annotation> processor : entry.getValue()) {
                    MetadataDefinition<?> metadataDefinition = processor.process(domainFunctionsClass, method, parameter, null);
                    if (metadataDefinition != null) {
                        metadataDefinitions.add(metadataDefinition);
                    }
                }
            } else {
                Annotation annotation = AnnotationUtils.findAnnotation(method, entry.getKey());
                if (annotation != null) {
                    for (DeclarativeFunctionParameterMetadataProcessor<Annotation> processor : entry.getValue()) {
                        MetadataDefinition<?> metadataDefinition = processor.process(domainFunctionsClass, method, parameter, annotation);
                        if (metadataDefinition != null) {
                            metadataDefinitions.add(metadataDefinition);
                        }
                    }
                }
            }
        }

        MetadataDefinition[] metadataDefinitionArray;
        if (metadataDefinitions.isEmpty()) {
            metadataDefinitionArray = EMPTY;
        } else {
            metadataDefinitionArray = metadataDefinitions.toArray(new MetadataDefinition[metadataDefinitions.size()]);
        }

        ResolvedType resolvedType = resolveType(typeName, elementTypeName, type, elementType, domainFunctionsClass, method, parameter);
        if (resolvedType.collection) {
            if (resolvedType.typeName.isEmpty()) {
                function.withArgument(parameterName, resolvedType.type, metadataDefinitionArray);
            } else {
                function.withArgument(parameterName, resolvedType.typeName, metadataDefinitionArray);
            }
        } else {
            if (resolvedType.typeName.isEmpty()) {
                function.withArgument(parameterName, resolvedType.type, metadataDefinitionArray);
            } else {
                function.withArgument(parameterName, resolvedType.typeName, metadataDefinitionArray);
            }
        }
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
        Set<Class<?>> superTypes = ReflectionUtils.getSuperTypes(domainTypeClass);

        // automatic metadata discovery via meta annotations
        List<MetadataDefinition<?>> metadataDefinitions = new ArrayList<>();

        for (Class<?> type : superTypes) {
            for (Map.Entry<Class<? extends Annotation>, List<DeclarativeMetadataProcessor<Annotation>>> entry : entityMetadataProcessors.entrySet()) {
                if (entry.getKey() == null) {
                    for (DeclarativeMetadataProcessor<Annotation> processor : entry.getValue()) {
                        MetadataDefinition<?> metadataDefinition = processor.process(domainTypeClass, null);
                        if (metadataDefinition != null) {
                            metadataDefinitions.add(metadataDefinition);
                        }
                    }
                } else {
                    Annotation annotation = AnnotationUtils.findAnnotation(type, entry.getKey());
                    if (annotation != null) {
                        for (DeclarativeMetadataProcessor<Annotation> processor : entry.getValue()) {
                            MetadataDefinition<?> metadataDefinition = processor.process(domainTypeClass, annotation);
                            if (metadataDefinition != null) {
                                metadataDefinitions.add(metadataDefinition);
                            }
                        }
                    }
                }
            }
        }

        if (domainTypeClass.isEnum()) {
            Class<? extends Enum<?>> enumDomainTypeClass = (Class<? extends Enum<?>>) domainTypeClass;
            EnumDomainTypeBuilder enumType = domainBuilder.createEnumType(name, enumDomainTypeClass);
            for (int i = 0; i < metadataDefinitions.size(); i++) {
                enumType.withMetadata(metadataDefinitions.get(i));
            }
            Enum[] enumConstants = (Enum[]) domainTypeClass.getEnumConstants();
            for (int i = 0; i < (enumConstants).length; i++) {
                handleEnumConstant(enumType, enumDomainTypeClass, enumConstants[i]);
            }
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
                            handleDomainAttributeMethod(entityType, domainTypeClass, method, implicitDiscovery);
                        }
                    }
                }
            }

            entityType.build();
        }

        return this;
    }

    private void handleEnumConstant(EnumDomainTypeBuilder enumType, Class<? extends Enum<?>> domainTypeClass, Enum<?> enumConstant) {
        // automatic metadata discovery via meta annotations
        List<MetadataDefinition<?>> metadataDefinitions = new ArrayList<>();
        Class<? extends Enum> constantClass = enumConstant.getClass();
        for (Map.Entry<Class<? extends Annotation>, List<DeclarativeMetadataProcessor<Annotation>>> entry : entityMetadataProcessors.entrySet()) {
            if (entry.getKey() == null) {
                for (DeclarativeMetadataProcessor<Annotation> processor : entry.getValue()) {
                    MetadataDefinition<?> metadataDefinition = processor.process(constantClass, null);
                    if (metadataDefinition != null) {
                        metadataDefinitions.add(metadataDefinition);
                    }
                }
            } else {
                Annotation annotation = AnnotationUtils.findAnnotation(constantClass, entry.getKey());
                if (annotation != null) {
                    for (DeclarativeMetadataProcessor<Annotation> processor : entry.getValue()) {
                        MetadataDefinition<?> metadataDefinition = processor.process(constantClass, annotation);
                        if (metadataDefinition != null) {
                            metadataDefinitions.add(metadataDefinition);
                        }
                    }
                }
            }
        }

        MetadataDefinition[] metadataDefinitionArray;
        if (metadataDefinitions.isEmpty()) {
            metadataDefinitionArray = EMPTY;
        } else {
            metadataDefinitionArray = metadataDefinitions.toArray(new MetadataDefinition[metadataDefinitions.size()]);
        }

        enumType.withValue(enumConstant.name(), metadataDefinitionArray);
    }

    private void handleDomainAttributeMethod(EntityDomainTypeBuilder entityType, Class<?> domainTypeClass, Method method, boolean implicitDiscovery) {
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
        Class<?> type = domainAttribute.collection() ? Collection.class : domainAttribute.value();
        String typeName = domainAttribute.collection() ? "Collection" : domainAttribute.typeName();
        Class<?> elementType = domainAttribute.collection() ? domainAttribute.value() : void.class;
        String elementTypeName = domainAttribute.collection() ? domainAttribute.typeName() : "";

        // automatic metadata discovery via meta annotations
        List<MetadataDefinition<?>> metadataDefinitions = new ArrayList<>();
        for (Map.Entry<Class<? extends Annotation>, List<DeclarativeAttributeMetadataProcessor<Annotation>>> entry : attributeMetadataProcessors.entrySet()) {
            if (entry.getKey() == DomainAttribute.class) {
                for (DeclarativeAttributeMetadataProcessor<Annotation> processor : entry.getValue()) {
                    MetadataDefinition<?> metadataDefinition = processor.process(domainTypeClass, method, domainAttribute);
                    if (metadataDefinition != null) {
                        metadataDefinitions.add(metadataDefinition);
                    }
                }
            } else if (entry.getKey() == null) {
                for (DeclarativeAttributeMetadataProcessor<Annotation> processor : entry.getValue()) {
                    MetadataDefinition<?> metadataDefinition = processor.process(domainTypeClass, method, null);
                    if (metadataDefinition != null) {
                        metadataDefinitions.add(metadataDefinition);
                    }
                }
            } else {
                Annotation annotation = AnnotationUtils.findAnnotation(method, entry.getKey());
                if (annotation != null) {
                    for (DeclarativeAttributeMetadataProcessor<Annotation> processor : entry.getValue()) {
                        MetadataDefinition<?> metadataDefinition = processor.process(domainTypeClass, method, annotation);
                        if (metadataDefinition != null) {
                            metadataDefinitions.add(metadataDefinition);
                        }
                    }
                }
            }
        }

        MetadataDefinition[] metadataDefinitionArray;
        if (metadataDefinitions.isEmpty()) {
            metadataDefinitionArray = EMPTY;
        } else {
            metadataDefinitionArray = metadataDefinitions.toArray(new MetadataDefinition[metadataDefinitions.size()]);
        }

        ResolvedType resolvedType = resolveType(typeName, elementTypeName, type, elementType, domainTypeClass, method, null);
        if (resolvedType.collection) {
            if (resolvedType.typeName.isEmpty()) {
                entityType.addCollectionAttribute(name, resolvedType.type, metadataDefinitionArray);
            } else {
                entityType.addCollectionAttribute(name, resolvedType.typeName, metadataDefinitionArray);
            }
        } else {
            if (resolvedType.typeName.isEmpty()) {
                entityType.addAttribute(name, resolvedType.type, metadataDefinitionArray);
            } else {
                entityType.addAttribute(name, resolvedType.typeName, metadataDefinitionArray);
            }
        }
    }

    protected ResolvedType resolveType(String typeName, String elementTypeName, Class<?> type, Class<?> elementType, Class<?> baseClass, Method method, Parameter parameter) {
        if (!typeName.isEmpty()) {
            if ("Collection".equals(typeName)) {
                if (elementTypeName.isEmpty()) {
                    return ResolvedType.collection(elementType);
                } else {
                    return ResolvedType.collection(elementTypeName);
                }
            } else {
                return ResolvedType.basic(typeName);
            }
        } else {
            Class<?> returnType;
            if (type == void.class) {
                if (typeResolver != null) {
                    Object resolvedType = typeResolver.resolve(method.getGenericReturnType());
                    if (resolvedType instanceof String) {
                        return ResolvedType.basic((String) resolvedType);
                    } else if (resolvedType instanceof Class<?>) {
                        return ResolvedType.basic((Class<?>) resolvedType);
                    } else if (resolvedType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) resolvedType;
                        Type rawType = parameterizedType.getRawType();
                        if ("Collection".equals(rawType.getTypeName()) || rawType instanceof Class<?> && Collection.class.isAssignableFrom((Class<?>) rawType)) {
                            Type[] typeArguments = parameterizedType.getActualTypeArguments();
                            if (typeArguments.length > 0) {
                                if (typeArguments[0] instanceof Class<?>) {
                                    return ResolvedType.collection((Class<?>) typeArguments[0]);
                                } else {
                                    return ResolvedType.collection(typeArguments[0].getTypeName());
                                }
                            }
                        } else if (rawType instanceof Class<?>) {
                            return ResolvedType.basic((Class<?>) rawType);
                        }
                    }
                }

                if (method == null) {
                    returnType = parameter.getType();
                    if (Collection.class.isAssignableFrom(returnType)) {
                        Type[] typeArguments = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments();
                        elementType = (Class<?>) ReflectionUtils.resolve(baseClass, typeArguments[0]);
                    } else {
                        elementType = null;
                    }
                } else {
                    returnType = ReflectionUtils.getResolvedMethodReturnType(baseClass, method);
                    if (Collection.class.isAssignableFrom(returnType)) {
                        Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(baseClass, method);
                        elementType = typeArguments[0];
                    } else {
                        elementType = null;
                    }
                }
            } else {
                returnType = type;
                elementType = null;
            }
            if (Collection.class.isAssignableFrom(returnType)) {
                return ResolvedType.collection(elementType);
            } else {
                return ResolvedType.basic(returnType);
            }
        }
    }

    private static class ResolvedType {
        private final String typeName;
        private final Class<?> type;
        private final boolean collection;

        public ResolvedType(String typeName, Class<?> type, boolean collection) {
            this.typeName = typeName;
            this.type = type;
            this.collection = collection;
        }

        public static ResolvedType basic(String typeName) {
            return new ResolvedType(typeName, null, false);
        }

        public static ResolvedType basic(Class<?> type) {
            return new ResolvedType("", type, false);
        }

        public static ResolvedType collection(String typeName) {
            return new ResolvedType(typeName, null, true);
        }

        public static ResolvedType collection(Class<?> type) {
            return new ResolvedType("", type, true);
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
