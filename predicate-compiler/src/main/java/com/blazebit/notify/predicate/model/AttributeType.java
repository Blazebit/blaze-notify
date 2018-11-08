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

package com.blazebit.notify.predicate.model;

import java.util.Arrays;
import java.util.List;


public enum AttributeType {
	NUMERIC(ComparisonOperatorType.values()),
	DATE(ComparisonOperatorType.values()),
	DATE_TIME(ComparisonOperatorType.values()),
	STRING(ComparisonOperatorType.EQUAL, ComparisonOperatorType.NOT_EQUAL),
	ENUM(ComparisonOperatorType.EQUAL, ComparisonOperatorType.NOT_EQUAL),
	COLLECTION();
	
	private final List<ComparisonOperatorType> allowedOperatorTypes;
	
	private AttributeType(ComparisonOperatorType... operatorTypes) {
		this.allowedOperatorTypes = Arrays.asList(operatorTypes);
	}

	public List<ComparisonOperatorType> getAllowedComparisonOperatorTypes() {
		return allowedOperatorTypes;
	}
}
