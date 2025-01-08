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
package com.blazebit.notify.email.message;

import javax.activation.DataSource;

/**
 * An E-Mail attachment.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class Attachment {
    private final String name;
    private final DataSource dataSource;

    /**
     * Creates a new attachment.
     *
     * @param name       The attachment name
     * @param dataSource The data source
     */
    public Attachment(String name, DataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
    }

    /**
     * Returns the attachment name.
     *
     * @return the attachment name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the attachment data source.
     *
     * @return the attachment data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }
}
