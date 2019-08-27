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

package com.blazebit.notify.actor.integration.cdi;

import com.blazebit.notify.actor.ScheduledActor;
import com.blazebit.notify.actor.declarative.ActorConfig;
import com.blazebit.notify.actor.declarative.DeclarativeActor;
import com.blazebit.notify.actor.declarative.DeclarativeActorContextBuilder;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DeclarativeActorExtension implements Extension {

    private final DeclarativeActorContextBuilder actorContextBuilder = DeclarativeActor.getDefaultProvider().createDefaultBuilder();
    private final List<RuntimeException> exceptions = new ArrayList<>();

    <X> void processEntityView(@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager) {
        if (pat.getAnnotatedType().isAnnotationPresent(ActorConfig.class)) {
            Class<X> javaClass = pat.getAnnotatedType().getJavaClass();
            if (!ScheduledActor.class.isAssignableFrom(javaClass)) {
                exceptions.add(new IllegalArgumentException("The actor class: " + javaClass.getName() + " does not implement ScheduledActor!"));
            } else {
                try {
                    Set<Bean<?>> beans = beanManager.getBeans(javaClass);
                    Bean<?> bean = beanManager.resolve(beans);
                    CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
                    ScheduledActor actor = (ScheduledActor) beanManager.getReference(bean, javaClass, creationalContext);
                    actorContextBuilder.addActor(actor);
                } catch (RuntimeException ex) {
                    exceptions.add(new IllegalArgumentException("Exception occurred while registering the actor class: " + javaClass.getName(), ex));
                }
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
        Class<?> beanClass = DeclarativeActorContextBuilder.class;
        Class<?>[] types = new Class[] { DeclarativeActorContextBuilder.class, Object.class };
        Annotation[] qualifiers = new Annotation[] { new DefaultLiteral()};
        Class<? extends Annotation> scope = Dependent.class;
        Bean<DeclarativeActorContextBuilder> bean = new CustomBean<DeclarativeActorContextBuilder>(beanClass, types, qualifiers, scope, actorContextBuilder);

        abd.addBean(bean);
    }
    
}
