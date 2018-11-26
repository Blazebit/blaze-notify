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

import java.util.List;
import java.util.Objects;

public class InPredicate extends AbstractPredicate {
    private final ArithmeticExpression left;
    private final List<ArithmeticExpression> inItems;

    public InPredicate(DomainType type, ArithmeticExpression left, List<ArithmeticExpression> inItems, boolean negated) {
        super(type, negated);
        this.left = left;
        this.inItems = inItems;
    }

    public ArithmeticExpression getLeft() {
        return left;
    }

    public List<ArithmeticExpression> getInItems() {
        return inItems;
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
        InPredicate that = (InPredicate) o;
        return Objects.equals(left, that.left) &&
                Objects.equals(inItems, that.inItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), left, inItems);
    }
}
