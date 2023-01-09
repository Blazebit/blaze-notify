/*
 * Copyright 2018 - 2023 Blazebit.
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
package com.blazebit.notify.channel.smtp;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.job.ConfigurationSource;
import com.blazebit.notify.ChannelFactory;
import com.blazebit.notify.ChannelKey;
import com.blazebit.notify.NotificationJobContext;

/**
 * A factory for SMTP channels.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@ServiceProvider(ChannelFactory.class)
public class SmtpChannelFactory implements ChannelFactory<SmtpChannel> {

    @Override
    public ChannelKey<SmtpChannel> getChannelType() {
        return SmtpChannel.KEY;
    }

    @Override
    public SmtpChannel createChannel(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
        return new SmtpChannel(configurationSource);
    }
}
