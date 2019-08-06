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

package com.blazebit.notify.expression.persistence;

import com.blazebit.notify.domain.persistence.EntityAttribute;
import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.domain.runtime.model.EntityDomainTypeAttribute;
import com.blazebit.notify.expression.*;
import com.blazebit.persistence.WhereBuilder;

import java.util.List;
import java.util.Map;

public class PersistenceExpressionSerializer implements Expression.Visitor, ExpressionSerializer<WhereBuilder<?>> {

    private final DomainModel domainModel;
    private final StringBuilder sb;
    private WhereBuilder<?> whereBuilder;
    private Context context;

    public PersistenceExpressionSerializer(DomainModel domainModel) {
        this.domainModel = domainModel;
        this.sb = new StringBuilder();
    }

    @Override
    public Context createContext(Map<String, Object> contextParameters) {
        return new Context() {
            @Override
            public Object getContextParameter(String contextParameterName) {
                return contextParameters.get(contextParameterName);
            }
        };
    }

    @Override
    public void serializeTo(Expression expression, WhereBuilder<?> target) {
        serializeTo(null, expression, target);
    }

    @Override
    public void serializeTo(Context newContext, Expression expression, WhereBuilder<?> target) {
        WhereBuilder old = whereBuilder;
        Context oldContext = context;
        whereBuilder = target;
        context = newContext;
        try {
            sb.setLength(0);
            // TODO: adapt BP to support this
            sb.append("CASE WHEN ");
            expression.accept(this);
            sb.append(" THEN 1 ELSE 0 END");
            target.whereSubqueries(sb.toString()).end().eqExpression("1");
        } finally {
            whereBuilder = old;
            context = oldContext;
        }
    }

    @Override
    public void visit(FunctionInvocation e) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void visit(Literal e) {
        if (e.getType().getJavaType() == String.class) {
            sb.append('\'').append(e.getValue()).append('\'');
        } else {
            sb.append(e.getValue());
        }
    }

    @Override
    public void visit(Path e) {
        String persistenceAlias = getPersistenceAlias(e.getAlias());
        sb.append(persistenceAlias);

        List<EntityDomainTypeAttribute> attributes = e.getAttributes();
        for (int i = 0; i < attributes.size(); i++) {
            EntityDomainTypeAttribute attribute = attributes.get(i);
            sb.append('.');
            sb.append(getPersistenceAttribute(attribute));
        }
    }

    protected String getPersistenceAttribute(EntityDomainTypeAttribute attribute) {
        EntityAttribute metadata = attribute.getMetadata(EntityAttribute.class);
        return metadata.getExpression();
    }

    protected String getPersistenceAlias(String alias) {
        Object o = context.getContextParameter(alias);
        if (o instanceof String) {
            return (String) o;
        }

        throw new IllegalStateException("The domain root object alias '" + alias + "' has no registered persistence alias!");
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
        if (e.getInItems().size() == 1 && e.getInItems().get(0) instanceof Path) {
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
