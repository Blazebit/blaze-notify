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


public class CompoundPredicate extends AbstractPredicate {

    private final boolean conjunction;
    private final List<Predicate> predicates;

    public CompoundPredicate(DomainType type, List<Predicate> predicates, boolean conjunction) {
        this(type, predicates, conjunction, false);
    }

    public CompoundPredicate(DomainType type, List<Predicate> predicates, boolean conjunction, boolean negated) {
        super(type, negated);
        this.predicates = predicates;
        this.conjunction = conjunction;
    }

    public boolean isConjunction() {
        return conjunction;
    }

    public List<Predicate> getPredicates() {
        return predicates;
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompoundPredicate)) {
            return false;
        }
        if (!super.equals(o)) return false;

        CompoundPredicate that = (CompoundPredicate) o;

        if (conjunction != that.conjunction) {
            return false;
        }
        return getPredicates().equals(that.getPredicates());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (conjunction ? 1 : 0);
        result = 31 * result + getPredicates().hashCode();
        return result;
    }
}
