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
package com.blazebit.notify.notification.channel.slack;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.notify.notification.ChannelFactory;
import com.blazebit.notify.notification.ChannelKey;
import com.blazebit.notify.job.ConfigurationSource;
import com.blazebit.notify.notification.NotificationJobContext;

@ServiceProvider(ChannelFactory.class)
public class SlackChannelFactory implements ChannelFactory<SlackChannel> {

    @Override
    public ChannelKey<SlackChannel> getChannelType() {
        return SlackChannel.KEY;
    }

    @Override
    public SlackChannel createChannel(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
        return new SlackChannel(configurationSource);
    }
}
