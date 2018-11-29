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
package com.blazebit.notify.template.freemarker;

import com.blazebit.notify.template.api.Template;

public class FreemarkerTemplate implements Template {

    public static final String TEMPLATE_TYPE = "freemarker";

    private final freemarker.template.Template freemarkerTemplate;

    public FreemarkerTemplate(freemarker.template.Template freemarkerTemplate) {
        this.freemarkerTemplate = freemarkerTemplate;
    }

    @Override
    public String getTemplateType() {
        return TEMPLATE_TYPE;
    }

    public freemarker.template.Template getFreemarkerTemplate() {
        return freemarkerTemplate;
    }
}
