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
package com.blazebit.notify.channel.smtp.james.module;

import com.google.inject.AbstractModule;
import org.apache.james.mailbox.lucene.search.LuceneMessageSearchIndex;
import org.apache.james.mailbox.store.search.ListeningMessageSearchIndex;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class MessageSearchIndexModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(ListeningMessageSearchIndex.class).to(LuceneMessageSearchIndex.class);
        this.bind(Directory.class).to(RAMDirectory.class);
    }
}
