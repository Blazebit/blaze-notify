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
package com.blazebit.notify.template.freemarker;

import freemarker.template.Template;

import java.io.Serializable;
import java.util.Locale;

/**
 * A lookup for localized Freemarker template.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface FreemarkerTemplateLookup extends Serializable {

    /**
     * Returns the freemarker template for the given locale.
     *
     * @param locale The locale
     * @return the template
     */
    public Template findTemplate(Locale locale);
}
