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

import com.blazebit.notify.predicate.model.DisjunctivePredicate;
import com.blazebit.notify.predicate.model.Predicate;

public class SimplePredicateVisitorImpl extends PredicateBaseVisitor<Predicate> {

    @Override
    public Predicate visitStart(PredicateParser.StartContext ctx) {
        List<Predicate> singleDisjunct = new ArrayList<>(1);
        singleDisjunct.add(ctx.conditional_expression().accept(this));
        return new DisjunctivePredicate(singleDisjunct);
    }

//	@Override
//	public Predicate visitBooleanTermExpression(PredicateParser.BooleanTermExpressionContext ctx) {
//		return wrapDisjunct(ctx.boolean_term().accept(this));
//	}
//
//	@Override
//	public SPLExpression visitRecursiveBooleanTerm(RecursiveBooleanTermContext ctx) {
//		return wrapConjunct(ctx.boolean_term().accept(this));
//	}
//
//	@Override
//	public SPLExpression visitRecursiveBooleanPrimary(RecursiveBooleanPrimaryContext ctx) {
//		return wrapConjunct(ctx.boolean_primary().accept(this));
//	}
//
//	@Override
//	public SPLExpression visitDisjunctiveExpression(DisjunctiveExpressionContext ctx) {
//		DisjunctiveExpression leftDisjunctiveExpression = (DisjunctiveExpression) ctx.boolean_expression().accept(this);
//		leftDisjunctiveExpression.getDisjuncts().add(ctx.boolean_term().accept(this));
//		return leftDisjunctiveExpression;
//	}
//
//	@Override
//	public SPLExpression visitConjunctiveExpression(ConjunctiveExpressionContext ctx) {
//		SPLExpression leftExpression = ctx.boolean_term().accept(this);
//		SPLExpression rightExpression = ctx.boolean_primary().accept(this);
//
//		if (leftExpression instanceof ConjunctiveExpression) {
//			ConjunctiveExpression leftConjunctiveExpression = (ConjunctiveExpression) leftExpression;
//			leftConjunctiveExpression.getConjuncts().add(rightExpression);
//			return leftConjunctiveExpression;
//		} else {
//			List<SPLExpression> conjuncts = new ArrayList<>();
//			conjuncts.add(leftExpression);
//			conjuncts.add(rightExpression);
//			return new ConjunctiveExpression(conjuncts);
//		}
//	}
//
//	@Override
//	public SPLExpression visitDateTimeBetweenExpression(DateTimeBetweenExpressionContext ctx) {
//		DateTimeAtom left = (DateTimeAtom) ctx.left.accept(this);
//		DateTimeAtom lower = (DateTimeAtom) ctx.lower.accept(this);
//		DateTimeAtom upper = (DateTimeAtom) ctx.upper.accept(this);
//		return new DateTimeBetweenExpression(left, upper, lower);
//	}
//
//	@Override
//	public SPLExpression visitArithmeticBetweenExpression(ArithmeticBetweenExpressionContext ctx) {
//		ArithmeticExpression left = (ArithmeticExpression) ctx.left.accept(this);
//		ArithmeticExpression lower = (ArithmeticExpression) ctx.lower.accept(this);
//		ArithmeticExpression upper = (ArithmeticExpression) ctx.upper.accept(this);
//		return new ArithmeticBetweenExpression(left, upper, lower);
//	}
//
//	@Override
//	public SPLExpression visitStringInExpression(StringInExpressionContext ctx) {
//		return new StringInLiteralExpression((StringAtom) ctx.string_expression().accept(this), getLiteralList(StringAtom.class, ctx.in_items), ctx.not != null);
//	}
//
//	@Override
//	public SPLExpression visitStringInCollectionExpression(StringInCollectionExpressionContext ctx) {
//		return new StringInAttributeExpression((StringAtom) ctx.string_expression().accept(this), (CollectionAtom) ctx.collection_attribute().accept(this), ctx.not != null);
//	}
//
//	@Override
//	public SPLExpression visitArithmeticInExpression(ArithmeticInExpressionContext ctx) {
//		return new ArithmeticInLiteralExpression((ArithmeticExpression) ctx.arithmetic_expression().accept(this), getLiteralList(ArithmeticFactor.class, ctx.in_items), ctx.not != null);
//	}
//
//	@Override
//	public SPLExpression visitArithmeticInCollectionExpression(ArithmeticInCollectionExpressionContext ctx) {
//		return new ArithmeticInAttributeExpression((ArithmeticExpression) ctx.arithmetic_expression().accept(this), (CollectionAtom) ctx.collection_attribute().accept(this), ctx.not != null);
//	}
//
//	@Override
//	public SPLExpression visitEnumMemberOfExpression(EnumMemberOfExpressionContext ctx) {
//		return new EnumInLiteralExpression((EnumAtom) ctx.enum_expression().accept(this), (EnumCollectionAtom) ctx.enum_collection_literal().accept(this), ctx.not != null);
//	}
//
//	@SuppressWarnings("unchecked")
//	private <T> List<T> getLiteralList(Class<T> clazz, List<? extends ParserRuleContext> items) {
//		List<T> literals = new ArrayList<>();
//		for (ParserRuleContext item : items) {
//			literals.add((T) item.accept(this));
//		}
//		return literals;
//	}
//
//	@Override
//	public SPLExpression visitDateTimeIsNullExpression(DateTimeIsNullExpressionContext ctx) {
//		return new IsNullExpression(ctx.left.accept(this), ctx.kind.getType() == SimpleSPLParser.IS_NOT_NULL);
//	}
//
//	@Override
//	public SPLExpression visitArithmeticIsNullExpression(ArithmeticIsNullExpressionContext ctx) {
//		return new IsNullExpression(ctx.left.accept(this), ctx.kind.getType() == SimpleSPLParser.IS_NOT_NULL);
//	}
//
//	@Override
//	public SPLExpression visitStringIsNullExpression(StringIsNullExpressionContext ctx) {
//		return new IsNullExpression(ctx.left.accept(this), ctx.kind.getType() == SimpleSPLParser.IS_NOT_NULL);
//	}
//
//	@Override
//	public SPLExpression visitEnumIsNullExpression(EnumIsNullExpressionContext ctx) {
//		return new IsNullExpression(ctx.left.accept(this), ctx.kind.getType() == SimpleSPLParser.IS_NOT_NULL);
//	}
//
//	@Override
//	public SPLExpression visitArithmeticComparisonExpression(ArithmeticComparisonExpressionContext ctx) {
//		return new SimpleComparisonExpression((ArithmeticExpression) ctx.left.accept(this), (ArithmeticExpression) ctx.right.accept(this), ComparisonOperatorType.valueOfOperator(ctx.comparison_operator().getText()));
//	}
//
//	@Override
//	public SPLExpression visitDateTimeComparisonExpression(DateTimeComparisonExpressionContext ctx) {
//		return new SimpleComparisonExpression((DateTimeAtom) ctx.left.accept(this), (DateTimeAtom) ctx.right.accept(this), ComparisonOperatorType.valueOfOperator(ctx.comparison_operator().getText()));
//	}
//
//	@Override
//	public SPLExpression visitStringComparisonExpression(StringComparisonExpressionContext ctx) {
//		return new SimpleComparisonExpression((StringAtom) ctx.left.accept(this), (StringAtom) ctx.right.accept(this), ComparisonOperatorType.valueOfOperator(ctx.equality_comparison_operator().getText()));
//	}
//
//	@Override
//	public SPLExpression visitEnumComparisonExpression(EnumComparisonExpressionContext ctx) {
//		return new SimpleComparisonExpression((EnumAtom) ctx.left.accept(this), (EnumAtom) ctx.right.accept(this), ComparisonOperatorType.valueOfOperator(ctx.equality_comparison_operator().getText()));
//	}
//
//	@Override
//	public SPLExpression visitAdditiveExpression(AdditiveExpressionContext ctx) {
//		ArithmeticExpression left = (ArithmeticExpression) ctx.arithmetic_expression().accept(this);
//		ArithmeticExpression right = (ArithmeticExpression) ctx.arithmetic_term().accept(this);
//		ArithmeticOperatorType operator = ArithmeticOperatorType.valueOfOperator(ctx.op.getText());
//
//		return new ChainingArithmeticExpression(left, right, operator);
//	}
//
//	@Override
//	public SPLExpression visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
//		ArithmeticExpression left = (ArithmeticExpression) ctx.arithmetic_term().accept(this);
//		ArithmeticExpression right = (ArithmeticExpression) ctx.arithmetic_factor().accept(this);
//		ArithmeticOperatorType operator = ArithmeticOperatorType.valueOfOperator(ctx.op.getText());
//
//		return new ChainingArithmeticExpression(left, right, operator);
//	}
//
//	@Override
//	public SPLExpression visitSimpleArithmeticPrimary(SimpleArithmeticPrimaryContext ctx) {
//		return new ArithmeticFactor((ArithmeticExpression) ctx.arithmetic_primary().accept(this), ctx.sign == null ? false : "-".equals(ctx.sign.getText()));
//	}
//
//	@Override
//	public SPLExpression visitArithmeticInItem(ArithmeticInItemContext ctx) {
//		return new ArithmeticFactor((ArithmeticAtom) ctx.arithmetic_atom().accept(this), ctx.sign == null ? false : "-".equals(ctx.sign.getText()));
//	}
//
//	@Override
//	public SPLExpression visitArithmeticPrimaryParanthesis(ArithmeticPrimaryParanthesisContext ctx) {
//		return ctx.arithmetic_expression().accept(this);
//	}
//
//	/*************************************************************************************
//	 * Literals
//	 *************************************************************************************/
//
//	@Override
//	public SPLExpression visitTimestampLiteral(TimestampLiteralContext ctx) {
//		return new DateTimeAtom(Literal.ofDateTimeString(ctx.TIMESTAMP_LITERAL().getText()));
//	}
//
//	@Override
//	public SPLExpression visitCurrentTimestamp(CurrentTimestampContext ctx) {
//		return new DateTimeAtom(true);
//	}
//
//	@Override
//	public SPLExpression visitStringLiteral(StringLiteralContext ctx) {
//		return new StringAtom(Literal.ofQuotedString(ctx.STRING_LITERAL().getText()));
//	}
//
//	@Override
//	public SPLExpression visitNumericLiteral(NumericLiteralContext ctx) {
//		return new ArithmeticAtom(Literal.ofNumericString(ctx.NUMERIC_LITERAL().getText()));
//	}
//
//	@Override
//	public SPLExpression visitEnumLiteral(EnumLiteralContext ctx) {
//		return new EnumAtom(Literal.ofEnumLiteral(ctx.ENUM_LITERAL().getText()));
//	}
//
//	@Override
//	public SPLExpression visitEnumCollectionLiteral(EnumCollectionLiteralContext ctx) {
//		return new EnumCollectionAtom(Literal.ofEnumCollectionLiteral(ctx.ENUM_COLLECTION_LITERAL().getText()));
//	}
//
//	/*************************************************************************************
//	 * Attributes - User
//	 *************************************************************************************/
//
//	@Override
//	public SPLExpression visitUser_string_attribute(User_string_attributeContext ctx) {
//		return new StringAtom(UserAttribute.valueOfAttribute(ctx.attr.getText()));
//	}
//
//	@Override
//	public SPLExpression visitUser_enum_attribute(User_enum_attributeContext ctx) {
//		return new EnumAtom(UserAttribute.valueOfAttribute(ctx.attr.getText()));
//	}
//
//	@Override
//	public SPLExpression visitUser_numeric_attribute(User_numeric_attributeContext ctx) {
//		return new ArithmeticAtom(UserAttribute.valueOfAttribute(ctx.attr.getText()));
//	}
//
//	@Override
//	public SPLExpression visitUser_date_time_attribute(User_date_time_attributeContext ctx) {
//		return new DateTimeAtom(UserAttribute.valueOfAttribute(ctx.attr.getText()));
//	}
//
//	@Override
//	public SPLExpression visitUser_collection_attribute(User_collection_attributeContext ctx) {
//		return new CollectionAtom(UserAttribute.valueOfAttribute(ctx.attr.getText()));
//	}
//
//    /*************************************************************************************
//	 * Attributes - UserDevice
//	 *************************************************************************************/
//
//
//	@Override
//	public Predicate visitUser_device_string_attribute(User_device_string_attributeContext ctx) {
//		return new StringAtom(UserDeviceAttribute.valueOfAttribute(ctx.attr.getText()));
//	}
//
//	@Override
//	public SPLExpression visitUser_device_enum_attribute(User_device_enum_attributeContext ctx) {
//		return new EnumAtom(UserDeviceAttribute.valueOfAttribute(ctx.attr.getText()));
//	}
//
//	@Override
//	public SPLExpression visitUser_device_numeric_attribute(User_device_numeric_attributeContext ctx) {
//		return new ArithmeticAtom(UserDeviceAttribute.valueOfAttribute(ctx.attr.getText()));
//	}
//
//	@Override
//	public SPLExpression visitUser_device_date_time_attribute(User_device_date_time_attributeContext ctx) {
//		return new DateTimeAtom(UserDeviceAttribute.valueOfAttribute(ctx.attr.getText()));
//	}
//
//	private SPLExpression wrapDisjunct(SPLExpression expression) {
//		if (expression instanceof DisjunctiveExpression) {
//			return expression;
//		}
//
//		List<SPLExpression> disjuncts = new ArrayList<>(1);
//		disjuncts.add(wrapConjunct(expression));
//		return new DisjunctiveExpression(disjuncts);
//	}
//
//	private SPLExpression wrapConjunct(SPLExpression expression) {
//		if (expression instanceof ConjunctiveExpression) {
//			return expression;
//		}
//
//		List<SPLExpression> conjuncts = new ArrayList<>(1);
//		conjuncts.add(expression);
//		return new ConjunctiveExpression(conjuncts);
//	}
}
