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

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;

public class TemporalInterval {

    private final int years;
    private final int months;
    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;

    public TemporalInterval(int years, int months, int days, int hours, int minutes, int seconds) {
        if (years < 0) {
            throw new IllegalArgumentException("Invalid negative years: " + years);
        }
        if (months < 0) {
            throw new IllegalArgumentException("Invalid negative months: " + months);
        }
        if (days < 0) {
            throw new IllegalArgumentException("Invalid negative days: " + days);
        }
        if (hours < 0) {
            throw new IllegalArgumentException("Invalid negative hours: " + hours);
        }
        if (minutes < 0) {
            throw new IllegalArgumentException("Invalid negative minutes: " + minutes);
        }
        if (seconds < 0) {
            throw new IllegalArgumentException("Invalid negative seconds: " + seconds);
        }
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (years != 0) {
            sb.append(years).append(" YEARS ");
        }
        if (months != 0) {
            sb.append(months).append(" MONTHS ");
        }
        if (days != 0) {
            sb.append(days).append(" DAYS ");
        }
        if (hours != 0) {
            sb.append(hours).append(" HOURS ");
        }
        if (minutes != 0) {
            sb.append(minutes).append(" MINUTES ");
        }
        sb.append(seconds).append(" SECONDS");
        return sb.toString();
    }

    public TemporalInterval add(TemporalInterval interval2) {
        return new TemporalInterval(
                this.years + interval2.years,
                this.months + interval2.months,
                this.days + interval2.days,
                this.hours + interval2.hours,
                this.minutes + interval2.minutes,
                this.seconds + interval2.seconds
        );
    }

    public TemporalInterval subtract(TemporalInterval interval2) {
        return new TemporalInterval(
                this.years - interval2.years,
                this.months - interval2.months,
                this.days - interval2.days,
                this.hours - interval2.hours,
                this.minutes - interval2.minutes,
                this.seconds - interval2.seconds
        );
    }

    public Instant add(Instant instant) {
        return instant.atZone(ZoneOffset.UTC)
                .plusYears(years)
                .plusMonths(months)
                .plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds)
                .toInstant();
    }

    public Instant subtract(Instant instant) {
        return instant.atZone(ZoneOffset.UTC)
                .minusYears(years)
                .minusMonths(months)
                .minusDays(days)
                .minusHours(hours)
                .minusMinutes(minutes)
                .minusSeconds(seconds)
                .toInstant();
    }
}
