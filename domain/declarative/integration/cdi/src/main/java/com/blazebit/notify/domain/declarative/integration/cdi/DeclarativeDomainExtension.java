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

package com.blazebit.notify.domain.declarative.integration.cdi;

import com.blazebit.notify.domain.declarative.DeclarativeDomain;
import com.blazebit.notify.domain.declarative.DeclarativeDomainConfiguration;
import com.blazebit.notify.domain.declarative.DomainFunctions;
import com.blazebit.notify.domain.declarative.DomainType;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DeclarativeDomainExtension implements Extension {

    private final DeclarativeDomainConfiguration configuration = DeclarativeDomain.getDefaultProvider().createDefaultConfiguration();
    private final List<RuntimeException> exceptions = new ArrayList<>();

    <X> void processEntityView(@Observes ProcessAnnotatedType<X> pat) {
        if (pat.getAnnotatedType().isAnnotationPresent(DomainType.class)) {
            try {
                configuration.addDomainType(pat.getAnnotatedType().getJavaClass());
            } catch (RuntimeException ex) {
                exceptions.add(new IllegalArgumentException("Exception occurred while reading domain type class: " + pat.getAnnotatedType().getJavaClass().getName(), ex));
            }
        }
        if (pat.getAnnotatedType().isAnnotationPresent(DomainFunctions.class)) {
            try {
                configuration.addDomainFunctions(pat.getAnnotatedType().getJavaClass());
            } catch (RuntimeException ex) {
                exceptions.add(new IllegalArgumentException("Exception occurred while reading domain type class: " + pat.getAnnotatedType().getJavaClass().getName(), ex));
            }
        }
    }
    
    void beforeBuild(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        if (!exceptions.isEmpty()) {
            for (RuntimeException exception : exceptions) {
                abd.addDefinitionError(exception);
            }
            return;
        }
        Class<?> beanClass = DeclarativeDomainConfiguration.class;
        Class<?>[] types = new Class[] { DeclarativeDomainConfiguration.class, Object.class };
        Annotation[] qualifiers = new Annotation[] { new DefaultLiteral()};
        Class<? extends Annotation> scope = Dependent.class;
        Bean<DeclarativeDomainConfiguration> bean = new CustomBean<DeclarativeDomainConfiguration>(beanClass, types, qualifiers, scope, configuration);

        abd.addBean(bean);
    }
    
}
