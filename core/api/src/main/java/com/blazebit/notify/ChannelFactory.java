/*
 * Copyright 2018 - 2022 Blazebit.
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

import com.blazebit.job.ConfigurationSource;

/**
 * A factory for channels of a specific type.
 *
 * @param <C> The channel type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ChannelFactory<C extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>>  {

    /**
     * Returns the channel type class.
     *
     * @return the channel type class
     */
    ChannelKey<C> getChannelType();

    /**
     * Creates a channel for the given notification job context and the given configuration source.
     *
     * @param jobContext The notification job context
     * @param configurationSource The configuration source
     * @return A new channel
     */
    C createChannel(NotificationJobContext jobContext, ConfigurationSource configurationSource);
}
