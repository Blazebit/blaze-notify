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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public final class ParameterSerializable implements Serializable {

    private static final long serialVersionUID = 1L;

    private Set<TimeFrame> executionTimeFrames;
    private Map<String, Serializable> parameters;

    public ParameterSerializable(Set<TimeFrame> executionTimeFrames, Map<String, Serializable> parameters) {
        this.executionTimeFrames = executionTimeFrames;
        this.parameters = parameters;
    }

    public Set<TimeFrame> getExecutionTimeFrames() {
        return executionTimeFrames;
    }

    public Map<String, Serializable> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParameterSerializable)) {
            return false;
        }

        ParameterSerializable that = (ParameterSerializable) o;

        if (executionTimeFrames != null ? !executionTimeFrames.equals(that.executionTimeFrames) : that.executionTimeFrames != null) {
            return false;
        }
        return parameters != null ? parameters.equals(that.parameters) : that.parameters == null;
    }

    @Override
    public int hashCode() {
        int result = executionTimeFrames != null ? executionTimeFrames.hashCode() : 0;
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }
}
