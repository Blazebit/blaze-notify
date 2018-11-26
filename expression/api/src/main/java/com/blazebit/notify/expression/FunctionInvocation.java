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
package com.blazebit.notify.expression;

import com.blazebit.notify.domain.runtime.model.DomainFunctionArgument;
import com.blazebit.notify.domain.runtime.model.DomainType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FunctionInvocation implements ArithmeticExpression {
    private final String functionName;
    private final Map<DomainFunctionArgument, Expression> arguments;
    private final DomainType type;

    public FunctionInvocation(String functionName, Map<DomainFunctionArgument, Expression> arguments, DomainType type) {
        this.functionName = functionName;
        this.arguments = arguments;
        this.type = type;
    }

    public String getFunctionName() {
        return functionName;
    }

    public Map<DomainFunctionArgument, Expression> getArguments() {
        return arguments;
    }

    @Override
    public DomainType getType() {
        return type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionInvocation that = (FunctionInvocation) o;
        return functionName.equals(that.functionName) &&
                arguments.equals(that.arguments) &&
                type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionName, arguments, type);
    }
}
