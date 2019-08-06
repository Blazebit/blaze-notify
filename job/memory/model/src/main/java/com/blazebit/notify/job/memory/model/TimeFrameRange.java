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

package com.blazebit.notify.job.memory.model;

import java.time.LocalTime;

public class TimeFrameRange extends AbstractTemporalFrameRange<LocalTime> {
	private static final long serialVersionUID = 1L;

	public TimeFrameRange() {
	}

	public TimeFrameRange(LocalTime start, LocalTime end) {
	    super(start, end);
	}

    @Override
    public int compareTo(AbstractTemporalFrameRange<LocalTime> o) {
        int cmp = start.compareTo(o.start);
        if (cmp != 0) {
            return cmp;
        }
        return end.compareTo(o.end);
    }

}
