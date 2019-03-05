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
import com.blazebit.notify.template.api.Template;

import javax.activation.DataSource;

public class StaticAttachmentTemplate implements Template {
    private final Attachment attachment;

    public StaticAttachmentTemplate(Attachment attachment) {
        this.attachment = attachment;
    }

    public StaticAttachmentTemplate(String attachmentName, DataSource dataSource) {
        this.attachment = new Attachment(attachmentName, dataSource);
    }

    public Attachment getAttachment() {
        return attachment;
    }
}