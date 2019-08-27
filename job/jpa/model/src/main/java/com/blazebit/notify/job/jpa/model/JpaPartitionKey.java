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

package com.blazebit.notify.job.jpa.model;

import com.blazebit.notify.job.JobInstance;
import com.blazebit.notify.job.PartitionKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface JpaPartitionKey extends PartitionKey {

	String getPartitionPredicate(String jobAlias);

	String getIdAttributeName();

	String getScheduleAttributeName();

	String getPartitionKeyAttributeName();

	String getStatePredicate(String jobAlias);

	Object getReadyStateValue();

	String getJoinFetches(String jobAlias);

	@Override
	default boolean matches(JobInstance<?> jobInstance) {
		throw new UnsupportedOperationException("A JpaPartitionKey does not need to support this!");
	}

	interface JpaPartitionKeyBuilder {
		JpaPartitionKeyBuilder withName(String name);
		JpaPartitionKeyBuilder withJobInstanceType(Class<? extends JobInstance<?>> jobInstanceType);
		JpaPartitionKeyBuilder withPartitionPredicateProvider(Function<String, String> partitionPredicateProvider);
		JpaPartitionKeyBuilder withIdAttributeName(String idAttributeName);
		JpaPartitionKeyBuilder withScheduleAttributeName(String scheduleAttributeName);
		JpaPartitionKeyBuilder withPartitionKeyAttributeName(String partitionKeyAttributeName);
		JpaPartitionKeyBuilder withStateAttributeName(String stateAttributeName);
		JpaPartitionKeyBuilder withReadyStateValue(Object readyStateValue);
		JpaPartitionKeyBuilder withJoinFetches(String... fetches);
		JpaPartitionKey build();
	}

	static JpaPartitionKeyBuilder builder() {
		return new JpaPartitionKeyBuilder() {
			String name0;
			Class<? extends JobInstance<?>> jobInstanceType0;
			Function<String, String> partitionPredicateProvider0;
			String idAttributeName0;
			String scheduleAttributeName0;
			String partitionKeyAttributeName0;
			String stateAttributeName0;
			Object readyStateValue0;
			List<String> fetches0 = new ArrayList<>();

			@Override
			public JpaPartitionKeyBuilder withName(String name) {
				this.name0 = name;
				return this;
			}

			@Override
			public JpaPartitionKeyBuilder withJobInstanceType(Class<? extends JobInstance<?>> jobInstanceType) {
				this.jobInstanceType0 = jobInstanceType;
				return this;
			}

			@Override
			public JpaPartitionKeyBuilder withPartitionPredicateProvider(Function<String, String> partitionPredicateProvider) {
				this.partitionPredicateProvider0 = partitionPredicateProvider;
				return this;
			}

			@Override
			public JpaPartitionKeyBuilder withIdAttributeName(String idAttributeName) {
				this.idAttributeName0 = idAttributeName;
				return this;
			}

			@Override
			public JpaPartitionKeyBuilder withScheduleAttributeName(String scheduleAttributeName) {
				this.scheduleAttributeName0 = scheduleAttributeName;
				return this;
			}

			@Override
			public JpaPartitionKeyBuilder withPartitionKeyAttributeName(String partitionKeyAttributeName) {
				this.partitionKeyAttributeName0 = partitionKeyAttributeName;
				return this;
			}

			@Override
			public JpaPartitionKeyBuilder withStateAttributeName(String stateAttributeName) {
				this.stateAttributeName0 = stateAttributeName;
				return this;
			}

			@Override
			public JpaPartitionKeyBuilder withReadyStateValue(Object readyStateValue) {
				this.readyStateValue0 = readyStateValue;
				return this;
			}

			@Override
			public JpaPartitionKeyBuilder withJoinFetches(String... fetches) {
				Collections.addAll(this.fetches0, fetches);
				return this;
			}

			@Override
			public JpaPartitionKey build() {
				return new JpaPartitionKey() {
					private final String name = name0;
					private final Class<? extends JobInstance<?>> jobInstanceType = jobInstanceType0;
					private final Function<String, String> partitionPredicateProvider = partitionPredicateProvider0;
					private final String idAttributeName = idAttributeName0;
					private final String scheduleAttributeName = scheduleAttributeName0;
					private final String partitionKeyAttributeName = partitionKeyAttributeName0;
					private final String stateAttributeName = stateAttributeName0;
					private final Object readyStateValue = readyStateValue0;
					private final String[] fetches = fetches0.toArray(new String[fetches0.size()]);

					@Override
					public Class<? extends JobInstance<?>> getJobInstanceType() {
						return jobInstanceType;
					}

					@Override
					public String getPartitionPredicate(String jobAlias) {
						return partitionPredicateProvider == null ? "" : partitionPredicateProvider.apply(jobAlias);
					}

					@Override
					public String getIdAttributeName() {
						return idAttributeName;
					}

					@Override
					public String getScheduleAttributeName() {
						return scheduleAttributeName;
					}

					@Override
					public String getPartitionKeyAttributeName() {
						return partitionKeyAttributeName;
					}

					@Override
					public String getStatePredicate(String jobAlias) {
						return jobAlias + "." + stateAttributeName + " = :readyState";
					}

					@Override
					public Object getReadyStateValue() {
						return readyStateValue;
					}

					@Override
					public String getJoinFetches(String jobAlias) {
						if (fetches.length == 0) {
							return "";
						}
						StringBuilder sb = new StringBuilder();
						String previousAlias = jobAlias;
						for (int i = 0; i < fetches.length; i++) {
							String fetch = fetches[i];
							sb.append(" LEFT JOIN FETCH ").append(previousAlias).append('.').append(fetch).append(' ').append(jobAlias).append(i);
							previousAlias = jobAlias + i;
						}

						return sb.toString();
					}

					@Override
					public String toString() {
						return name;
					}
				};
			}
		};
	}
}
