/*
 * Copyright 2018 - 2025 Blazebit.
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
package com.blazebit.notify.channel.memory;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.job.ConfigurationSource;
import com.blazebit.notify.ChannelFactory;
import com.blazebit.notify.ChannelKey;
import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.NotificationMessage;
import com.blazebit.notify.NotificationRecipient;

/**
 * A factory for in-memory channels.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@ServiceProvider(ChannelFactory.class)
public class MemoryChannelFactory implements ChannelFactory<MemoryChannel<NotificationRecipient<?>, NotificationMessage>> {
    @Override
    public ChannelKey<MemoryChannel<NotificationRecipient<?>, NotificationMessage>> getChannelType() {
        return MemoryChannel.KEY;
    }

    @Override
    public MemoryChannel<NotificationRecipient<?>, NotificationMessage> createChannel(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
        return new MemoryChannel<>();
    }
}
