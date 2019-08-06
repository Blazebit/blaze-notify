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
package com.blazebit.notify.template.api;

import java.util.function.Function;

public interface ConfigurationSource {

    Object getProperty(String property);

    public default <T> T getOrFail(String key, Class<T> type, Function<String, T> stringConverter) {
        return getOrDefault(key, type, stringConverter, o -> {
            if (o == null) {
                throw new TemplateException("Missing a value for configuration property " + key + "!");
            } else {
                throw new TemplateException("Invalid value for configuration property " + key + "! Expected value of type " + type.getName() + " but got: " + o);
            }
        });
    }

    public default <T> T getOrDefault(String key, Class<T> type, Function<String, T> stringConverter, Function<Object, T> defaultFunction) {
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
