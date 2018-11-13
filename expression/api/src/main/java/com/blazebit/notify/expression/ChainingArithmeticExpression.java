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

public class ChainingArithmeticExpression implements ArithmeticExpression {
    private final ArithmeticExpression left;
    private final ArithmeticExpression right;
    private final ArithmeticOperatorType operator;

    public ChainingArithmeticExpression(ArithmeticExpression left, ArithmeticExpression right, ArithmeticOperatorType operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public ArithmeticExpression getLeft() {
        return left;
    }

    public ArithmeticExpression getRight() {
        return right;
    }

    public ArithmeticOperatorType getOperator() {
        return operator;
    }

    @Override
    public DomainType getType() {
        return left.getType();
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
        ChainingArithmeticExpression that = (ChainingArithmeticExpression) o;
        return Objects.equals(left, that.left) &&
                Objects.equals(right, that.right) &&
                operator == that.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right, operator);
    }
}
	
