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
package com.blazebit.notify.email.model.memory;

/**
 * The review state of an E-Mail.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public enum EmailNotificationReviewState {
    /**
     * It's unnecessary to review the E-Mail.
     */
    UNNECESSARY,
    /**
     * A review of the E-Mail is necessary.
     */
    NECESSARY,
    /**
     * The E-Mail was already reviewed.
     */
    REVIEWED;
}
