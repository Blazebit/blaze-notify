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
package com.blazebit.notify.template.api;

import java.util.function.Function;

/**
 * A configuration source.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ConfigurationSource {

    /**
     * Returns the property value registered for the given property key.
     *
     * @param property The property key
     * @return The value or <code>null</code>
     */
    Object getProperty(String property);

    /**
     * Invokes {@link #getPropertyOrDefault(String, Class, Function, Function)} with a default function that throws an exception.
     *
     * @param key             The property key
     * @param type            The desired type
     * @param stringConverter The converter to use if the value is not of the desired type
     * @param <T>             The desired type
     * @return The value
     * @throws TemplateException if there is no value for the property or of the wrong type
     */
    public default <T> T getPropertyOrFail(String key, Class<T> type, Function<String, T> stringConverter) {
        return getPropertyOrDefault(key, type, stringConverter, o -> {
            if (o == null) {
                throw new TemplateException("Missing a value for configuration property " + key + "!");
            } else {
                throw new TemplateException("Invalid value for configuration property " + key + "! Expected value of type " + type.getName() + " but got: " + o);
            }
        });
    }

    /**
     * Returns the property value returned by {@link #getProperty(String)} after trying to convert to the given type,
     * optionally with a string converter and if not found, provides the value as given via the default function.
     *
     * @param key             The property key
     * @param type            The desired type
     * @param stringConverter The converter to use if the value is not of the desired type
     * @param defaultFunction The function to invoke for a default value if there is no value available
     * @param <T>             The desired type
     * @return The value or whatever the default function provides
     */
    public default <T> T getPropertyOrDefault(String key, Class<T> type, Function<String, T> stringConverter, Function<Object, T> defaultFunction) {
        Object o = getProperty(key);
        if (o == null) {
            return defaultFunction.apply(o);
        }
        if (!type.isInstance(o)) {
            if (stringConverter != null) {
                return stringConverter.apply(o.toString());
            }
            return defaultFunction.apply(o);
        }

        return type.cast(o);
    }
}
