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

import com.blazebit.notify.predicate.model.*;
import org.antlr.v4.runtime.ParserRuleContext;

public class PredicateVisitorImpl extends PredicateParserBaseVisitor<Expression> {

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
    public Predicate visitArithmeticComparisonPredicate(PredicateParser.ArithmeticComparisonPredicateContext ctx) {
        return new ComparisonPredicate((ArithmeticExpression) ctx.left.accept(this), (ArithmeticExpression) ctx.right.accept(this), ComparisonOperatorType.valueOfOperator(ctx.comparison_operator().getText()));
    }

    @Override
    public Predicate visitDateTimeComparisonPredicate(PredicateParser.DateTimeComparisonPredicateContext ctx) {
        return new ComparisonPredicate((DateTimeAtom) ctx.left.accept(this), (DateTimeAtom) ctx.right.accept(this), ComparisonOperatorType.valueOfOperator(ctx.comparison_operator().getText()));
    }

    @Override
    public Predicate visitStringComparisonPredicate(PredicateParser.StringComparisonPredicateContext ctx) {
        return new ComparisonPredicate((StringAtom) ctx.left.accept(this), (StringAtom) ctx.right.accept(this), ComparisonOperatorType.valueOfOperator(ctx.equality_comparison_operator().getText()));
    }

    @Override
    public Predicate visitEnumComparisonPredicate(PredicateParser.EnumComparisonPredicateContext ctx) {
        return new ComparisonPredicate((EnumAtom) ctx.left.accept(this), (EnumAtom) ctx.right.accept(this), ComparisonOperatorType.valueOfOperator(ctx.equality_comparison_operator().getText()));
    }

    @Override
    public Predicate visitDateTimeBetweenPredicate(PredicateParser.DateTimeBetweenPredicateContext ctx) {
        DateTimeAtom left = (DateTimeAtom) ctx.left.accept(this);
		DateTimeAtom lower = (DateTimeAtom) ctx.lower.accept(this);
		DateTimeAtom upper = (DateTimeAtom) ctx.upper.accept(this);
		return new DateTimeBetweenPredicate(left, upper, lower);
    }

    @Override
    public Predicate visitArithmeticBetweenPredicate(PredicateParser.ArithmeticBetweenPredicateContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.left.accept(this);
		ArithmeticExpression lower = (ArithmeticExpression) ctx.lower.accept(this);
		ArithmeticExpression upper = (ArithmeticExpression) ctx.upper.accept(this);
		return new ArithmeticBetweenPredicate(left, upper, lower);
    }

    @Override
    public Predicate visitStringInPredicate(PredicateParser.StringInPredicateContext ctx) {
        return new StringInPredicate((StringAtom) ctx.string_expression().accept(this), getLiteralList(StringAtom.class, ctx.in_items), ctx.not != null);
    }

    @Override
    public Predicate visitStringInCollectionPredicate(PredicateParser.StringInCollectionPredicateContext ctx) {
        return new StringInCollectionPredicate((StringAtom) ctx.string_expression().accept(this), (CollectionAtom) ctx.collection_attribute().accept(this), ctx.not != null);
    }

    @Override
    public Predicate visitArithmeticInPredicate(PredicateParser.ArithmeticInPredicateContext ctx) {
        return new ArithmeticInPredicate((ArithmeticExpression) ctx.arithmetic_expression().accept(this), getLiteralList(ArithmeticFactor.class, ctx.in_items), ctx.not != null);
    }

    @Override
    public Predicate visitArithmeticInCollectionPredicate(PredicateParser.ArithmeticInCollectionPredicateContext ctx) {
        return new ArithmeticInCollectionPredicate((ArithmeticExpression) ctx.arithmetic_expression().accept(this), (CollectionAtom) ctx.collection_attribute().accept(this), ctx.not != null);
    }

    @Override
    public Predicate visitEnumInPredicate(PredicateParser.EnumInPredicateContext ctx) {
        return new EnumInPredicate((EnumAtom) ctx.enum_expression().accept(this), getLiteralList(EnumAtom.class, ctx.in_items), ctx.not != null);
    }

    @Override
    public Predicate visitEnumInCollectionPredicate(PredicateParser.EnumInCollectionPredicateContext ctx) {
        return new ArithmeticInCollectionPredicate((ArithmeticExpression) ctx.enum_expression().accept(this), (CollectionAtom) ctx.collection_attribute().accept(this), ctx.not != null);
    }

    @Override
    public Predicate visitDateTimeIsNullPredicate(PredicateParser.DateTimeIsNullPredicateContext ctx) {
        return new IsNullPredicate(ctx.left.accept(this), ctx.kind.getType() == PredicateParser.IS_NOT_NULL);
    }

    @Override
    public Predicate visitArithmeticIsNullPredicate(PredicateParser.ArithmeticIsNullPredicateContext ctx) {
        return new IsNullPredicate(ctx.left.accept(this), ctx.kind.getType() == PredicateParser.IS_NOT_NULL);
    }

    @Override
    public Predicate visitStringIsNullPredicate(PredicateParser.StringIsNullPredicateContext ctx) {
        return new IsNullPredicate(ctx.left.accept(this), ctx.kind.getType() == PredicateParser.IS_NOT_NULL);
    }

    @Override
    public Predicate visitEnumIsNullPredicate(PredicateParser.EnumIsNullPredicateContext ctx) {
        return new IsNullPredicate(ctx.left.accept(this), ctx.kind.getType() == PredicateParser.IS_NOT_NULL);
    }

    @Override
    public Expression visitAdditiveExpression(PredicateParser.AdditiveExpressionContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.arithmetic_expression().accept(this);
		ArithmeticExpression right = (ArithmeticExpression) ctx.arithmetic_term().accept(this);
		ArithmeticOperatorType operator = ArithmeticOperatorType.valueOfOperator(ctx.op.getText());

		return new ChainingArithmeticExpression(left, right, operator);
    }

    @Override
    public Expression visitMultiplicativeExpression(PredicateParser.MultiplicativeExpressionContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.arithmetic_term().accept(this);
		ArithmeticExpression right = (ArithmeticExpression) ctx.arithmetic_factor().accept(this);
		ArithmeticOperatorType operator = ArithmeticOperatorType.valueOfOperator(ctx.op.getText());

		return new ChainingArithmeticExpression(left, right, operator);
    }

    @Override
    public Expression visitSimpleArithmeticPrimary(PredicateParser.SimpleArithmeticPrimaryContext ctx) {
        return new ArithmeticFactor((ArithmeticExpression) ctx.arithmetic_primary().accept(this), ctx.sign != null && ctx.sign.getType() == PredicateParser.OP_MINUS);
    }

    @Override
    public Expression visitArithmeticInItem(PredicateParser.ArithmeticInItemContext ctx) {
        return new ArithmeticFactor((ArithmeticAtom) ctx.arithmetic_atom().accept(this), ctx.sign != null && ctx.sign.getType() == PredicateParser.OP_MINUS);
    }

    @Override
    public Expression visitArithmeticPrimaryParanthesis(PredicateParser.ArithmeticPrimaryParanthesisContext ctx) {
        return ctx.arithmetic_expression().accept(this);
    }

    @Override
    public Expression visitTimestampLiteral(PredicateParser.TimestampLiteralContext ctx) {
        return new DateTimeAtom(Literal.ofDateTimeString(ctx.TIMESTAMP_LITERAL().getText()));
    }

    @Override
    public Expression visitCurrentTimestamp(PredicateParser.CurrentTimestampContext ctx) {
        return new DateTimeAtom(true);
    }

    @Override
    public Expression visitStringLiteral(PredicateParser.StringLiteralContext ctx) {
        return new StringAtom(Literal.ofQuotedString(ctx.STRING_LITERAL().getText()));
    }

    @Override
    public Expression visitEnumLiteral(PredicateParser.EnumLiteralContext ctx) {
        String enumName = unquote(ctx.enumName.getText());
        String enumKey = unquote(ctx.enumKey.getText());
        return new EnumAtom(Literal.of(new EnumValue(enumName, enumKey)));
    }

    @Override
    public Expression visitNumericLiteral(PredicateParser.NumericLiteralContext ctx) {
        return new ArithmeticAtom(Literal.ofNumericString(ctx.NUMERIC_LITERAL().getText()));
    }


    @Override
    public Expression visitDatetimeAttribute(PredicateParser.DatetimeAttributeContext ctx) {
        return new DateTimeAtom(new Attribute(ctx.identifier().getText(), TermType.DATE_TIME));
    }

    @Override
    public Expression visitStringAttribute(PredicateParser.StringAttributeContext ctx) {
        return new StringAtom(new Attribute(ctx.identifier().getText(), TermType.STRING));
    }

    @Override
    public Expression visitEnumAttribute(PredicateParser.EnumAttributeContext ctx) {
        return new EnumAtom(new Attribute(ctx.identifier().getText(), TermType.ENUM));
    }

    @Override
    public Expression visitNumericAttribute(PredicateParser.NumericAttributeContext ctx) {
        return new ArithmeticAtom(new Attribute(ctx.identifier().getText(), TermType.NUMERIC));
    }

    @Override
    public Expression visitCollectionAttribute(PredicateParser.CollectionAttributeContext ctx) {
        return new CollectionAtom(new Attribute(ctx.identifier().getText(), TermType.COLLECTION));
    }

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
}
