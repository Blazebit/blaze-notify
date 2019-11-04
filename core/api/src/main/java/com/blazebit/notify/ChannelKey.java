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

package com.blazebit.notify;

/**
 * A type safe wrapper for identifying channels by their channel type.
 *
 * @param <T> The channel type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ChannelKey<T> {

    /**
     * Returns the channel type identifier.
     *
     * @return the channel type identifier
     */
    String getChannelType();

    /**
     * Returns the channel class.
     *
     * @return the channel class
     */
    Class<T> getChannelClass();

    /**
     * Returns a new channel key for the given type identifier and the given class.
     *
     * @param type The channel type identifier
     * @param clazz The channel class
     * @param <T> The channel type
     * @return A new channel key
     */
    static <T> ChannelKey<T> of(String type, Class<T> clazz) {
        return new ChannelKey<T>() {
            @Override
            public String getChannelType() {
                return type;
            }

            @Override
            public Class<T> getChannelClass() {
                return clazz;
            }
        };
    }
}
