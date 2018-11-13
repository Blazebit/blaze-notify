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

import java.util.Objects;

public class ComparisonPredicate extends AbstractPredicate {
    private final TermExpression left;
    private final TermExpression right;
    private final ComparisonOperator operator;

    public ComparisonPredicate(TermExpression left, TermExpression right, ComparisonOperator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public TermExpression getLeft() {
        return left;
    }

    public TermExpression getRight() {
        return right;
    }

    public ComparisonOperator getOperator() {
        return operator;
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
        if (!super.equals(o)) return false;
        ComparisonPredicate that = (ComparisonPredicate) o;
        return Objects.equals(left, that.left) &&
                Objects.equals(right, that.right) &&
                operator == that.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), left, right, operator);
    }
}
