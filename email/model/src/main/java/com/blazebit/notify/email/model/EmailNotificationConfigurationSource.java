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
package com.blazebit.notify.email.model;

import com.blazebit.job.ConfigurationSource;
import com.blazebit.notify.email.message.EmailNotificationMessageResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link AbstractEmailNotification} based {@link ConfigurationSource}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailNotificationConfigurationSource implements ConfigurationSource {

    private final Map<String, Object> properties;

    /**
     * Creates a configuration source from the given {@link AbstractEmailNotification}.
     *
     * @param emailNotification The E-Mail notification
     */
    public EmailNotificationConfigurationSource(AbstractEmailNotification<?> emailNotification) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(EmailNotificationMessageResolver.EMAIL_MESSAGE_FROM_PROPERTY, emailNotification.getFrom().getEmail());
        properties.put(EmailNotificationMessageResolver.EMAIL_MESSAGE_FROM_NAME_PROPERTY, emailNotification.getFrom().getName());
        properties.put(EmailNotificationMessageResolver.EMAIL_MESSAGE_REPLY_TO_PROPERTY, emailNotification.getFrom().getReplyToEmail());
        properties.put(EmailNotificationMessageResolver.EMAIL_MESSAGE_REPLY_TO_NAME_PROPERTY, emailNotification.getFrom().getReplyToName());
//        properties.put(EmailNotificationMessageResolver.EMAIL_MESSAGE_ENVELOP_FROM_PROPERTY, null);
//        properties.put(EmailNotificationMessageResolver.EMAIL_MESSAGE_RESOURCE_BUNDLE_PROPERTY, null);

//        properties.put(EmailNotificationMessageResolver.EMAIL_TEMPLATE_CONTEXT_PROPERTY, null);
        properties.put(EmailNotificationMessageResolver.EMAIL_TEMPLATE_PROCESSOR_FACTORY_PROPERTY, emailNotification.getTemplateProcessorType());

        properties.put(EmailNotificationMessageResolver.EMAIL_MESSAGE_SUBJECT_PROPERTY, emailNotification.getSubjectTemplateProcessor());
        properties.put(EmailNotificationMessageResolver.EMAIL_MESSAGE_TEXT_PROPERTY, emailNotification.getBodyTextTemplateProcessor());
        properties.put(EmailNotificationMessageResolver.EMAIL_MESSAGE_HTML_PROPERTY, emailNotification.getBodyHtmlTemplateProcessor());
        properties.put(EmailNotificationMessageResolver.EMAIL_MESSAGE_ATTACHMENT_PROCESSORS_PROPERTY, emailNotification.getAttachmentProcessor());
        this.properties = properties;
    }

    @Override
    public Object getProperty(String property) {
        return properties.get(property);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmailNotificationConfigurationSource)) {
            return false;
        }

        EmailNotificationConfigurationSource that = (EmailNotificationConfigurationSource) o;

        return properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }
}
