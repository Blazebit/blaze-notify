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
package com.blazebit.notify.domain.runtime.model;

import java.util.Objects;

public class TemporalInterval {
    private final int years;
    private final int months;
    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;

    public TemporalInterval(int years, int months, int days, int hours, int minutes, int seconds) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemporalInterval interval = (TemporalInterval) o;
        return years == interval.years &&
                months == interval.months &&
                days == interval.days &&
                hours == interval.hours &&
                minutes == interval.minutes &&
                seconds == interval.seconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(years, months, days, hours, minutes, seconds);
    }
}
