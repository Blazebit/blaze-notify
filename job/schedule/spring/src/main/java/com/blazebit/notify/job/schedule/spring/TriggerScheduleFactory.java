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
package com.blazebit.notify.job.schedule.spring;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.notify.job.Schedule;
import com.blazebit.notify.job.spi.ScheduleFactory;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@ServiceProvider(ScheduleFactory.class)
public class TriggerScheduleFactory implements ScheduleFactory {

    private static final DateTimeFormatter CRON_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.SECOND_OF_MINUTE)
            .appendLiteral(' ')
            .appendValue(ChronoField.MINUTE_OF_HOUR)
            .appendLiteral(' ')
            .appendValue(ChronoField.HOUR_OF_DAY)
            .appendLiteral(' ')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral(' ')
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral(" ? ")
            .toFormatter();

    @Override
    public String asCronExpression(Instant instant) {
        return CRON_FORMATTER.format(instant.atOffset(ZoneOffset.UTC));
    }

    @Override
    public Schedule createSchedule(String cronExpression) {
        return new TriggerSchedule(new CronTrigger(cronExpression));
    }
}
