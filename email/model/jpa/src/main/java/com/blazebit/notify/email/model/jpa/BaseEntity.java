/*
 * Copyright 2018 - 2019 Blazebit.
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
package com.blazebit.notify.email.model.jpa;

import java.io.Serializable;

/**
 * The base type for entities.
 *
 * @param <I> The id type of the entity
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class BaseEntity<I extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private I id;

    /**
     * Creates an empty entity.
     */
    public BaseEntity() {
    }

    /**
     * Creates an entity with the given id.
     *
     * @param id The id
     */
    public BaseEntity(I id) {
        this.id = id;
    }

    /**
     * Returns the id.
     *
     * @return the id
     */
    protected I id() {
        return id;
    }

    /**
     * Returns the id.
     *
     * @return the id
     */
    public abstract I getId();

    /**
     * Sets the given id.
     *
     * @param id The id
     */
    public void setId(I id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getNoProxyClass(getClass()) != getNoProxyClass(obj.getClass())) {
            return false;
        }
        BaseEntity<?> other = (BaseEntity<?>) obj;
        // null does not equal null in case of ids!
        if (id == null || other.id == null) {
            return false;
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the non-proxy class.
     *
     * @param clazz The class
     * @return the non-proxy class
     */
    protected static Class<?> getNoProxyClass(Class<?> clazz) {
        while (clazz.getName().contains("javassist")) {
            clazz = clazz.getSuperclass();
        }

        return clazz;
    }
}
