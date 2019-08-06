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

package com.blazebit.notify.job;

import java.time.*;
import java.util.List;
import java.util.Set;

public interface TimeFrame {

	static Instant getNearestTimeFrameSchedule(Set<? extends TimeFrame> timeFrames, Instant time) {
		if (timeFrames == null || timeFrames.isEmpty()) {
			return time;
		}

		Instant earliestInstant = Instant.MAX;
		for (TimeFrame timeFrame : timeFrames) {
			Instant instant = timeFrame.getEarliestInstant(time);
			if (instant != null) {
				if (instant.equals(time)) {
					// Special case. When we find a time frame that contains the given time, we just return that time
					return time;
				}
				earliestInstant = earliestInstant.isBefore(instant) ? earliestInstant : instant;
			}
		}

		return earliestInstant;
	}

	static boolean isContained(Set<? extends TimeFrame> publishTimeFrames, Instant time) {
		if (publishTimeFrames != null) {
			for (TimeFrame publishTimeFrame : publishTimeFrames) {
				if (!publishTimeFrame.contains(time)) {
					return false;
				}
			}
		}
		return true;
	}

	public Year getStartYear();

	public Year getEndYear();

	public Month getStartMonth();

	public Month getEndMonth();

	public DayOfWeek getWeekDay();

	public LocalTime getStartTime();

	public LocalTime getEndTime();

	default boolean contains(Instant time) {
		OffsetDateTime offsetDateTime = time.atOffset(ZoneOffset.UTC);
		if (getStartYear() != null && getEndYear() != null) {
			int year = offsetDateTime.getYear();
			if (year < getStartYear().getValue() || year > getEndYear().getValue()) {
				return false;
			}
		}

		if (getStartMonth() != null && getEndMonth() != null) {
			int year = offsetDateTime.getMonthValue();
			if (year < getStartMonth().getValue() || year > getEndMonth().getValue()) {
				return false;
			}
		}

		if (getWeekDay() != null && !getWeekDay().equals(offsetDateTime.getDayOfWeek())) {
			return false;
		}

		if (getStartTime() != null && getEndTime() != null) {
			LocalTime localTime = offsetDateTime.toLocalTime();
			if (localTime.isBefore(getStartTime()) || localTime.isAfter(getEndTime())) {
				return false;
			}
		}

		return true;
	}

	default Instant getEarliestInstant() {
		return getEarliestInstant(Instant.now());
	}

	default Instant getEarliestInstant(Instant fromInstant) {
		OffsetDateTime offsetDateTime = fromInstant.atOffset(ZoneOffset.UTC);
		if (getStartTime() != null) {
			if (getStartTime().isAfter(offsetDateTime.toLocalTime())) {
				// Start time is in the future, so let's use that
				offsetDateTime = offsetDateTime.with(getStartTime());
			} else if (getEndTime() == null || getEndTime().isBefore(offsetDateTime.toLocalTime())) {
				// End time is in the future, so the current time is ok
			} else {
				// End time is in the past, so we need to set start time and adjust the day
				offsetDateTime = offsetDateTime.withDayOfMonth(offsetDateTime.getDayOfMonth() + 1).with(getStartTime());
			}
		}

		if (getStartYear() == null) {
			// No start year means we can start at any time
			offsetDateTime = TimeFrameUtils.adjustWeekDay(offsetDateTime, getWeekDay());
			offsetDateTime = TimeFrameUtils.adjustMonth(offsetDateTime, getEndYear(), getStartMonth(), getEndMonth(), getWeekDay());
		} else {
			if (getStartYear().getValue() > offsetDateTime.getYear()) {
				// Start year is in the future, so let's use that
				offsetDateTime = offsetDateTime.withYear(getStartYear().getValue());
				offsetDateTime = TimeFrameUtils.adjustWeekDay(offsetDateTime, getWeekDay());
				offsetDateTime = TimeFrameUtils.adjustMonth(offsetDateTime, getEndYear(), getStartMonth(), getEndMonth(), getWeekDay());
			} else if (getEndYear() == null || getEndYear().getValue() <= offsetDateTime.getYear()) {
				// End year is in the future, so the current year is ok
				return offsetDateTime.toInstant();
			} else {
				// End year is in the past, so we are done here as the constraints aren't satisfiable
				throw new IllegalStateException("Unsatisfiable constraints!");
			}
		}

		if (offsetDateTime == null) {
			return null;
		}
		return offsetDateTime.toInstant();
	}
}
