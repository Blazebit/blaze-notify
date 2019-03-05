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
package com.blazebit.notify.notification.channel.smtp.template;

import com.blazebit.notify.notification.channel.smtp.Attachment;
import com.blazebit.notify.template.api.TemplateProcessor;

import java.util.Map;

public class StaticAttachmentTemplateProcessor<T extends StaticAttachmentTemplate> implements TemplateProcessor<T, Attachment> {
    @Override
    public Attachment processTemplate(T template, Map<String, Object> model) {
        return template.getAttachment();
    }

    @Override
    public Class<Attachment> getTemplateProcessingResultType() {
        return Attachment.class;
    }
}
