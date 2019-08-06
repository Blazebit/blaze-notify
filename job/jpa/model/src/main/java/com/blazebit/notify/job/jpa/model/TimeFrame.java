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

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.*;
import java.util.List;

@Embeddable
public class TimeFrame implements com.blazebit.notify.job.TimeFrame, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The first year(inclusive) for which this time frame is active, null if independent of the start year.
	 */
	private Year startYear;
	/**
	 * The last year(inclusive) for which this time frame is active, null if independent of the end year.
	 */
	private Year endYear;
	/**
	 * The first month(inclusive) for which this time frame is active, null if independent of the start month.
	 */
	private Month startMonth;
	/**
	 * The last month(inclusive) for which this time frame is active, null if independent of the end month.
	 */
	private Month endMonth;
	/**
	 * The day of the week for which this time frame is active, null if independent of the day of week.
	 */
	private DayOfWeek weekDay;
	/**
	 * The time of day for which this time frame begins to be active, null if independent of the start time of day.
	 */
	private LocalTime startTime;
	/**
	 * The time of day for which this time frame ends to be active, null if independent of the end time of day.
	 */
	private LocalTime endTime;

	public TimeFrame() {
	}
	
	public Year getStartYear() {
		return startYear;
	}

	public void setStartYear(Year startYear) {
		this.startYear = startYear;
	}

	public Year getEndYear() {
		return endYear;
	}

	public void setEndYear(Year endYear) {
		this.endYear = endYear;
	}

	public Month getStartMonth() {
		return startMonth;
	}

	public void setStartMonth(Month startMonth) {
		this.startMonth = startMonth;
	}

	public Month getEndMonth() {
		return endMonth;
	}

	public void setEndMonth(Month endMonth) {
		this.endMonth = endMonth;
	}

	public DayOfWeek getWeekDay() {
		return weekDay;
	}

	public void setWeekDay(DayOfWeek weekDay) {
		this.weekDay = weekDay;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((endMonth == null) ? 0 : endMonth.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((endYear == null) ? 0 : endYear.hashCode());
		result = prime * result
				+ ((startMonth == null) ? 0 : startMonth.hashCode());
		result = prime * result
				+ ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result
				+ ((startYear == null) ? 0 : startYear.hashCode());
		result = prime * result + ((weekDay == null) ? 0 : weekDay.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeFrame other = (TimeFrame) obj;
		if (endMonth != other.endMonth)
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (endYear == null) {
			if (other.endYear != null)
				return false;
		} else if (!endYear.equals(other.endYear))
			return false;
		if (startMonth != other.startMonth)
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (startYear == null) {
			if (other.startYear != null)
				return false;
		} else if (!startYear.equals(other.startYear))
			return false;
		if (weekDay != other.weekDay)
			return false;
		return true;
	}

}
