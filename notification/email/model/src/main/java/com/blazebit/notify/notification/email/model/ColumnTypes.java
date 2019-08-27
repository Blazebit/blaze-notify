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
package com.blazebit.notify.notification.email.model;

/**
 * This class holds the values for the database column types.
 */
public final class ColumnTypes {

	public static final String MAIL_SUBJECT = "VARCHAR(255)";
	public static final String MAIL_CONTENT = "TEXT";
	public static final String MAIL_ADDRESS = "CITEXT";
	public static final String MAIL_RECIPIENT = "VARCHAR(255)";
	public static final String MAIL_MESSAGE_ID = "VARCHAR(60)";
	public static final String MAIL_NOTIFICATION = "TEXT";
}
