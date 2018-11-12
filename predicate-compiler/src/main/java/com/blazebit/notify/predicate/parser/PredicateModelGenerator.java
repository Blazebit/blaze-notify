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

package com.blazebit.notify.predicate.parser;

import java.util.ArrayList;
import java.util.List;

import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.domain.runtime.model.DomainOperator;
import com.blazebit.notify.domain.runtime.model.DomainPredicateType;
import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.predicate.model.*;
import org.antlr.v4.runtime.ParserRuleContext;

public class PredicateModelGenerator extends PredicateParserBaseVisitor<Expression> {

    private final DomainModel domainModel;
    private final LiteralFactory literalFactory;

    public PredicateModelGenerator(DomainModel domainModel, LiteralFactory literalFactory) {
        this.domainModel = domainModel;
        this.literalFactory = literalFactory;
    }

    @Override
    public Predicate visitStart(PredicateParser.StartContext ctx) {
        return (Predicate) ctx.conditional_expression().accept(this);
    }

    @Override
    public Predicate visitOrPredicate(PredicateParser.OrPredicateContext ctx) {
        Predicate leftExpression = (Predicate) ctx.left.accept(this);
        Predicate rightTerm = (Predicate) ctx.term.accept(this);

        DisjunctivePredicate disjunctivePredicate;
        if (leftExpression instanceof DisjunctivePredicate && !leftExpression.isNegated()) {
            disjunctivePredicate = (DisjunctivePredicate) leftExpression;
            disjunctivePredicate.getDisjuncts().add(rightTerm);
        } else {
            disjunctivePredicate = new DisjunctivePredicate(new ArrayList<Predicate>(2));
            disjunctivePredicate.getDisjuncts().add(leftExpression);
            disjunctivePredicate.getDisjuncts().add(rightTerm);
        }
        return disjunctivePredicate;
    }

    @Override
    public Predicate visitAndPredicate(PredicateParser.AndPredicateContext ctx) {
        Predicate leftTerm = (Predicate) ctx.left.accept(this);
        Predicate rightFactor = (Predicate) ctx.factor.accept(this);

        ConjunctivePredicate conjunctivePredicate;
        if (leftTerm instanceof ConjunctivePredicate && !leftTerm.isNegated()) {
            conjunctivePredicate = (ConjunctivePredicate) leftTerm;
            conjunctivePredicate.getConjuncts().add(rightFactor);
        } else {
            conjunctivePredicate = new ConjunctivePredicate(new ArrayList<Predicate>(2));
            conjunctivePredicate.getConjuncts().add(leftTerm);
            conjunctivePredicate.getConjuncts().add(rightFactor);
        }
        return conjunctivePredicate;
    }

    @Override
    public Predicate visitConditional_factor(PredicateParser.Conditional_factorContext ctx) {
        Predicate predicate = (Predicate) ctx.expr.accept(this);
        if (ctx.not != null) {
            predicate.setNegated(!predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Predicate visitComparisonPredicate(PredicateParser.ComparisonPredicateContext ctx) {
        TermExpression left = (TermExpression) ctx.left.accept(this);
        TermExpression right = (TermExpression) ctx.right.accept(this);
        ComparisonOperator comparisonOperator = ComparisonOperator.valueOfOperator(ctx.comparison_operator().getText());

        if (left.getType() == right.getType() && left.getType().getEnabledPredicates().contains(comparisonOperator.getDomainPredicateType())) {
            return new ComparisonPredicate(left, right, comparisonOperator);
        } else {
            throw typeError(left.getType(), right.getType(), comparisonOperator.getDomainPredicateType());
        }
    }

    @Override
    public Expression visitInPredicate(PredicateParser.InPredicateContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.arithmetic_expression().accept(this);
        List<ArithmeticExpression> inItems = getLiteralList(ArithmeticExpression.class, ctx.in_items);

        if (!left.getType().getEnabledPredicates().contains(DomainPredicateType.EQUALITY)) {
            throw new TypeErrorException(String.format("Type %s does not support predicate type %s", left.getType(), DomainPredicateType.EQUALITY));
        }
        for (TermExpression inItem : inItems) {
            if (left.getType() != inItem.getType()) {
                throw new TypeErrorException(String.format("Types %s and %s do not match", left.getType(), inItem.getType()));
            }
        }

        return new InPredicate(left, inItems, ctx.not != null);
    }

    @Override
    public Predicate visitBetweenPredicate(PredicateParser.BetweenPredicateContext ctx) {
        TermExpression left = (TermExpression) ctx.left.accept(this);
		TermExpression lower = (TermExpression) ctx.lower.accept(this);
		TermExpression upper = (TermExpression) ctx.upper.accept(this);

		if (left.getType() == lower.getType() && left.getType() == upper.getType() && left.getType().getEnabledPredicates().contains(DomainPredicateType.RELATIONAL)) {
            return new BetweenPredicate(left, upper, lower);
        } else {
		    throw new TypeErrorException(String.format("%s BETWEEN %s AND %s", left.getType(), lower.getType(), upper.getType()));
        }
    }

//    @Override
//    public Predicate visitStringInCollectionPredicate(PredicateParser.StringInCollectionPredicateContext ctx) {
//        return new StringInCollectionPredicate((StringAtom) ctx.string_expression().accept(this), (CollectionAtom) ctx.collection_attribute().accept(this), ctx.not != null);
//    }

    @Override
    public Predicate visitIsNullPredicate(PredicateParser.IsNullPredicateContext ctx) {
        TermExpression left = (TermExpression) ctx.left.accept(this);
        if (left.getType().getEnabledPredicates().contains(DomainPredicateType.NULLNESS)) {
            return new IsNullPredicate(ctx.left.accept(this), ctx.kind.getType() == PredicateParser.IS_NOT_NULL);
        } else {
            throw new TypeErrorException(String.format("Type %s does not support predicate type %s", left.getType(), DomainPredicateType.NULLNESS));
        }
    }

    @Override
    public Expression visitAdditiveExpression(PredicateParser.AdditiveExpressionContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.arithmetic_expression().accept(this);
		ArithmeticExpression right = (ArithmeticExpression) ctx.arithmetic_term().accept(this);
		ArithmeticOperatorType operator = ArithmeticOperatorType.valueOfOperator(ctx.op.getText());

		if (left.getType() == right.getType() && left.getType().getEnabledOperators().contains(operator.getDomainOperator())) {
		    return new ChainingArithmeticExpression(left, right, operator);
        } else {
            throw typeError(left.getType(), right.getType(), operator.getDomainOperator());
        }
    }

    @Override
    public Expression visitMultiplicativeExpression(PredicateParser.MultiplicativeExpressionContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.arithmetic_term().accept(this);
		ArithmeticExpression right = (ArithmeticExpression) ctx.arithmetic_factor().accept(this);
		ArithmeticOperatorType operator = ArithmeticOperatorType.valueOfOperator(ctx.op.getText());

		if (left.getType() == right.getType() && left.getType().getEnabledOperators().contains(operator.getDomainOperator())) {
		    return new ChainingArithmeticExpression(left, right, operator);
        } else {
            throw typeError(left.getType(), right.getType(), operator.getDomainOperator());
        }
    }

    @Override
    public Expression visitArithmeticPrimary(PredicateParser.ArithmeticPrimaryContext ctx) {
        return new ArithmeticFactor((ArithmeticExpression) ctx.arithmetic_primary().accept(this), ctx.sign != null && ctx.sign.getType() == PredicateParser.OP_MINUS);
    }

    @Override
    public Expression visitArithmeticInItem(PredicateParser.ArithmeticInItemContext ctx) {
        Atom atom = (Atom) ctx.atom().accept(this);

        if (ctx.sign == null) {
            return atom;
        } else {
            if (atom.getType().getEnabledOperators().contains(DomainOperator.UNARY_MINUS)) {
                return new ArithmeticFactor(atom, ctx.sign.getType() == PredicateParser.OP_MINUS);
            } else {
                throw new TypeErrorException(String.format("%s not enabled for type %s", DomainOperator.UNARY_MINUS, atom.getType()));
            }
        }
    }

    @Override
    public Expression visitArithmeticPrimaryParanthesis(PredicateParser.ArithmeticPrimaryParanthesisContext ctx) {
        return ctx.arithmetic_expression().accept(this);
    }

    @Override
    public Expression visitTimestampLiteral(PredicateParser.TimestampLiteralContext ctx) {
        return new Atom(literalFactory.ofDateTimeString(ctx.TIMESTAMP_LITERAL().getText()));
    }

    @Override
    public Expression visitCurrentTimestamp(PredicateParser.CurrentTimestampContext ctx) {
        return new Atom(literalFactory.currentTimestamp());
    }

    @Override
    public Expression visitStringLiteral(PredicateParser.StringLiteralContext ctx) {
        return new Atom(literalFactory.ofQuotedString(ctx.STRING_LITERAL().getText()));
    }

    @Override
    public Expression visitEnumLiteral(PredicateParser.EnumLiteralContext ctx) {
        String enumName = unquote(ctx.enumName.getText());
        String enumKey = unquote(ctx.enumKey.getText());
        return new Atom(literalFactory.ofEnumValue(new EnumValue(enumName, enumKey)));
    }

    @Override
    public Expression visitNumericLiteral(PredicateParser.NumericLiteralContext ctx) {
        return new Atom(literalFactory.ofNumericString(ctx.NUMERIC_LITERAL().getText()));
    }


    @Override
    public Expression visitDomainAttribute(PredicateParser.DomainAttributeContext ctx) {
        String domainModelIdentifier = ctx.identifier().getText();
        return new Atom(new Attribute(ctx.identifier().getText(), domainModel.getType(domainModelIdentifier)));
    }

//    @Override
//    public Expression visitCollectionAttribute(PredicateParser.CollectionAttributeContext ctx) {
//        return new CollectionAtom(new Attribute(ctx.identifier().getText(), TermType.COLLECTION));
//    }

	@SuppressWarnings("unchecked")
	private <T> List<T> getLiteralList(Class<T> clazz, List<? extends ParserRuleContext> items) {
		List<T> literals = new ArrayList<>();
		for (ParserRuleContext item : items) {
			literals.add((T) item.accept(this));
		}
		return literals;
	}

	private String unquote(String quotedString) {
        return quotedString.substring(1, quotedString.length() - 1);
    }

    private TypeErrorException typeError(DomainType t1, DomainType t2, DomainOperator operator) {
        return new TypeErrorException(String.format("%s %s %s", t1, operator, t2));
    }

    private TypeErrorException typeError(DomainType t1, DomainType t2, DomainPredicateType predicateType) {
        return new TypeErrorException(String.format("%s %s %s", t1, predicateType, t2));
    }
}
