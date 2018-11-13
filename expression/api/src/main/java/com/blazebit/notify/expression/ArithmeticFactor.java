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

import com.blazebit.notify.domain.runtime.model.DomainType;

import java.util.Objects;

public class ArithmeticFactor implements ArithmeticExpression {
    private final ArithmeticExpression expression;
    private final boolean invertSignum;

    public ArithmeticFactor(ArithmeticExpression expression, boolean invertSignum) {
        this.expression = expression;
        this.invertSignum = invertSignum;
    }

    public ArithmeticExpression getExpression() {
        return expression;
    }

    public boolean isInvertSignum() {
        return invertSignum;
    }

    @Override
    public DomainType getType() {
        return expression.getType();
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
        ArithmeticFactor that = (ArithmeticFactor) o;
        return invertSignum == that.invertSignum &&
                Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, invertSignum);
    }
}
