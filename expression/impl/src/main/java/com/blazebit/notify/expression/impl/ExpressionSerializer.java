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

package com.blazebit.notify.expression.impl;

import com.blazebit.notify.expression.*;

import java.util.List;

public class ExpressionSerializer implements Expression.Visitor {

    private StringBuilder sb;

    public ExpressionSerializer() {
        this(new StringBuilder());
    }

    public ExpressionSerializer(StringBuilder sb) {
        this.sb = sb;
    }

    public String serialize(Expression e) {
        e.accept(this);
        return sb.toString();
    }

    @Override
    public void visit(Attribute e) {

    }

    @Override
    public void visit(Atom e) {

    }

    @Override
    public void visit(ArithmeticFactor e) {
        if (e.isInvertSignum()) {
            sb.append('-');
        }
        e.getExpression().accept(this);
    }

    @Override
    public void visit(ChainingArithmeticExpression e) {
        e.getLeft().accept(this);
        sb.append(' ');
        sb.append(e.getOperator().getOperator());
        sb.append(' ');
        e.getRight().accept(this);
    }

    @Override
    public void visit(BetweenPredicate e) {
        boolean negated = e.isNegated();
        if (negated) {
            sb.append("NOT(");
        }
        e.getLeft().accept(this);
        sb.append(" BETWEEN ");
        e.getLower().accept(this);
        sb.append(" AND ");
        e.getUpper().accept(this);
        if (negated) {
            sb.append(')');
        }
    }

    @Override
    public void visit(InPredicate e) {
        e.getLeft().accept(this);
        if (e.isNegated()) {
            sb.append(" NOT");
        }
        sb.append(" IN ");
        if (e.getInItems().size() == 1 && e.getInItems().get(0) instanceof Attribute) {
            e.getInItems().get(0).accept(this);
        } else {
            sb.append('(');
            for (ArithmeticExpression inItem : e.getInItems()) {
                inItem.accept(this);
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append(')');
        }
    }

    @Override
    public void visit(ConjunctivePredicate e) {
        boolean negated = e.isNegated();
        if (negated) {
            sb.append("NOT(");
        }
        List<Predicate> conjuncts = e.getConjuncts();
        int size = conjuncts.size();
        Predicate predicate = conjuncts.get(0);
        if (predicate instanceof DisjunctivePredicate) {
            sb.append('(');
            predicate.accept(this);
            sb.append(')');
        } else {
            predicate.accept(this);
        }
        for (int i = 1; i < size; i++) {
            predicate = conjuncts.get(i);
            sb.append(" AND ");
            if (predicate instanceof DisjunctivePredicate && !predicate.isNegated()) {
                sb.append('(');
                predicate.accept(this);
                sb.append(')');
            } else {
                predicate.accept(this);
            }
        }
        if (negated) {
            sb.append(')');
        }
    }

    @Override
    public void visit(DisjunctivePredicate e) {
        boolean negated = e.isNegated();
        if (negated) {
            sb.append("NOT(");
        }
        List<Predicate> conjuncts = e.getDisjuncts();
        int size = conjuncts.size();
        conjuncts.get(0).accept(this);
        for (int i = 1; i < size; i++) {
            sb.append(" OR ");
            conjuncts.get(i).accept(this);
        }
        if (negated) {
            sb.append(')');
        }
    }

    @Override
    public void visit(ComparisonPredicate e) {
        boolean negated = e.isNegated();
        if (negated) {
            sb.append("NOT(");
        }
        e.getLeft().accept(this);
        sb.append(' ');
        sb.append(e.getOperator().getOperator());
        sb.append(' ');
        e.getRight().accept(this);
        if (negated) {
            sb.append(')');
        }
    }

    @Override
    public void visit(IsNullPredicate e) {
        e.getLeft().accept(this);
        sb.append(" IS ");
        if (e.isNegated()) {
            sb.append("NOT ");
        }
        sb.append("NULL");
    }
}
