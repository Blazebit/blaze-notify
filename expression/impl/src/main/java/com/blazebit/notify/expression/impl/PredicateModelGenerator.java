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

import com.blazebit.notify.domain.runtime.model.*;
import com.blazebit.notify.expression.*;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;

public class PredicateModelGenerator extends PredicateParserBaseVisitor<Expression> {

    private final DomainModel domainModel;
    private final LiteralFactory literalFactory;
    private final ExpressionCompiler.Context compileContext;
    private DomainType cachedBooleanDomainType;

    public PredicateModelGenerator(DomainModel domainModel, LiteralFactory literalFactory, ExpressionCompiler.Context compileContext) {
        this.domainModel = domainModel;
        this.literalFactory = literalFactory;
        this.compileContext = compileContext;
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
            disjunctivePredicate = new DisjunctivePredicate(getBooleanDomainType(), new ArrayList<Predicate>(2));
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
            conjunctivePredicate = new ConjunctivePredicate(getBooleanDomainType(), new ArrayList<Predicate>(2));
            conjunctivePredicate.getConjuncts().add(leftTerm);
            conjunctivePredicate.getConjuncts().add(rightFactor);
        }
        return conjunctivePredicate;
    }

    @Override
    public Expression visitNestedPredicate(PredicateParser.NestedPredicateContext ctx) {
        return ctx.expr.accept(this);
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
        ArithmeticExpression left = (ArithmeticExpression) ctx.left.accept(this);
        ArithmeticExpression right = (ArithmeticExpression) ctx.right.accept(this);
        ComparisonOperator comparisonOperator = ComparisonOperator.valueOfOperator(ctx.comparison_operator().getText());

        if (left.getType() == right.getType() && left.getType().getEnabledPredicates().contains(comparisonOperator.getDomainPredicateType())) {
            return new ComparisonPredicate(getBooleanDomainType(), left, right, comparisonOperator);
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
        for (ArithmeticExpression inItem : inItems) {
            if (left.getType() != inItem.getType()) {
                throw new TypeErrorException(String.format("Types %s and %s do not match", left.getType(), inItem.getType()));
            }
        }

        return new InPredicate(getBooleanDomainType(), left, inItems, ctx.not != null);
    }

    @Override
    public Predicate visitBetweenPredicate(PredicateParser.BetweenPredicateContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.left.accept(this);
        ArithmeticExpression lower = (ArithmeticExpression) ctx.lower.accept(this);
        ArithmeticExpression upper = (ArithmeticExpression) ctx.upper.accept(this);

        if (left.getType() == lower.getType() && left.getType() == upper.getType() && left.getType().getEnabledPredicates().contains(DomainPredicateType.RELATIONAL)) {
            return new BetweenPredicate(getBooleanDomainType(), left, upper, lower);
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
        ArithmeticExpression left = (ArithmeticExpression) ctx.left.accept(this);
        if (left.getType().getEnabledPredicates().contains(DomainPredicateType.NULLNESS)) {
            return new IsNullPredicate(getBooleanDomainType(), left, ctx.kind.getType() == PredicateParser.IS_NOT_NULL);
        } else {
            throw new TypeErrorException(String.format("Type %s does not support predicate type %s", left.getType(), DomainPredicateType.NULLNESS));
        }
    }

    @Override
    public Expression visitAdditiveExpression(PredicateParser.AdditiveExpressionContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.arithmetic_expression().accept(this);
        ArithmeticExpression right = (ArithmeticExpression) ctx.arithmetic_term().accept(this);
        ArithmeticOperatorType operator = ArithmeticOperatorType.valueOfOperator(ctx.op.getText());

        List<DomainType> operandTypes = Arrays.asList(left.getType(), right.getType());
        DomainOperationTypeResolver operationTypeResolver = domainModel.getOperationTypeResolver(left.getType().getName(), operator.getDomainOperator());
        if (operationTypeResolver == null) {
            throw cannotResolveOperationType(operator.getDomainOperator(), operandTypes);
        } else {
            DomainType domainType = operationTypeResolver.resolveType(domainModel, operandTypes);
            if (domainType == null) {
                throw cannotResolveOperationType(operator.getDomainOperator(), operandTypes);
            } else {
                return new ChainingArithmeticExpression(left, right, operator);
            }
        }
    }

    @Override
    public Expression visitMultiplicativeExpression(PredicateParser.MultiplicativeExpressionContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.arithmetic_term().accept(this);
        ArithmeticExpression right = (ArithmeticExpression) ctx.arithmetic_factor().accept(this);
        ArithmeticOperatorType operator = ArithmeticOperatorType.valueOfOperator(ctx.op.getText());

        List<DomainType> operandTypes = Arrays.asList(left.getType(), right.getType());
        DomainOperationTypeResolver operationTypeResolver = domainModel.getOperationTypeResolver(left.getType().getName(), operator.getDomainOperator());
        if (operationTypeResolver == null) {
            throw cannotResolveOperationType(operator.getDomainOperator(), operandTypes);
        } else {
            DomainType domainType = operationTypeResolver.resolveType(domainModel, operandTypes);
            if (domainType == null) {
                throw cannotResolveOperationType(operator.getDomainOperator(), operandTypes);
            } else {
                return new ChainingArithmeticExpression(left, right, operator);
            }
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
            return new ArithmeticFactor(atom, false);
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
        return new Atom(literalFactory.ofDateTimeString(ctx.content.getText()));
    }

    @Override
    public Expression visitTemporalIntervalLiteral(PredicateParser.TemporalIntervalLiteralContext ctx) {
        return new Atom(literalFactory.ofTemporalIntervalString(ctx.content.getText()));
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
    public Expression visitAttribute(PredicateParser.AttributeContext ctx) {
        String entityName = ctx.entityName.getText();
        String attributeName = ctx.attributeName.getText();
        DomainType type = domainModel.getType(entityName);
        if (type == null) {
            throw unknownEntityType(entityName);
        } else if (type instanceof EntityDomainType) {
            EntityDomainType entityType = (EntityDomainType) type;
            EntityDomainTypeAttribute attribute = entityType.getAttribute(attributeName);
            return new Atom(new Attribute(entityName, attributeName, attribute.getType()));
        } else {
            throw noEntityDomainType(entityName);
        }
    }

    @Override
    public Expression visitNoargFunctionInvocation(PredicateParser.NoargFunctionInvocationContext ctx) {
        String functionName = ctx.functionName.getText();
        DomainFunction function = domainModel.getFunction(ctx.identifier().getText());
        if (function == null) {
            throw unknownFunction(functionName);
        } else {
            DomainFunctionTypeResolver functionTypeResolver = domainModel.getFunctionTypeResolver(functionName);
            DomainType functionType = functionTypeResolver.resolveType(domainModel, function, Collections.<DomainFunctionArgument, DomainType>emptyMap());
            return new Atom(new FunctionInvocation(functionName, Collections.<Expression>emptyList(), functionType));
        }
    }

    @Override
    public Expression visitFunctionInvocation(PredicateParser.FunctionInvocationContext ctx) {
        String functionName = ctx.functionName.getText();
        DomainFunction function = domainModel.getFunction(functionName);
        if (function == null) {
            throw unknownFunction(functionName);
        } else {
            List<Expression> arguments = getLiteralList(Expression.class, ctx.args);
            if (arguments.size() > function.getArgumentCount()) {
                throw new DomainModelException(String.format("Function '%s' expects at most %d arguments but found %d",
                        function.getName(),
                        function.getArgumentCount(),
                        arguments.size()
                ));
            }
            Map<DomainFunctionArgument, DomainType> argumentTypes = new HashMap<>(arguments.size());
            for (int i = 0; i < arguments.size(); i++) {
                DomainFunctionArgument domainFunctionArgument = function.getArguments().get(i);
                argumentTypes.put(domainFunctionArgument, arguments.get(i).getType());
            }
            DomainFunctionTypeResolver functionTypeResolver = domainModel.getFunctionTypeResolver(functionName);
            DomainType functionType = functionTypeResolver.resolveType(domainModel, function, argumentTypes);
            return new Atom(new FunctionInvocation(functionName, arguments, functionType));
        }
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

    private DomainType getBooleanDomainType() {
        if (cachedBooleanDomainType == null) {
            cachedBooleanDomainType = domainModel.getType(Boolean.class);
            if (cachedBooleanDomainType == null) {
                throw new DomainModelException("No domain type defined for type " + Boolean.class.getName());
            }
        }
        return cachedBooleanDomainType;
    }

    private TypeErrorException typeError(DomainType t1, DomainType t2, DomainOperator operator) {
        return new TypeErrorException(String.format("%s %s %s", t1, operator, t2));
    }

    private DomainModelException missingOperationTypeResolver(DomainType type, DomainOperator operator) {
        return new DomainModelException(String.format("Missing operation type resolver for type %s and operator %s", type, operator));
    }

    private TypeErrorException typeError(DomainType t1, DomainType t2, DomainPredicateType predicateType) {
        return new TypeErrorException(String.format("%s %s %s", t1, predicateType, t2));
    }

    private DomainModelException unknownEntityType(String identifier) {
        return new DomainModelException(String.format("Undefined entity '%s'", identifier));
    }

    private DomainModelException unknownFunction(String identifier) {
        return new DomainModelException(String.format("Undefined function '%s'", identifier));
    }

    private TypeErrorException noEntityDomainType(String identifier) {
        return new TypeErrorException(String.format("Resolved type for identifier %s is no instance of %s", identifier, EntityDomainType.class));
    }

    private TypeErrorException cannotResolveOperationType(DomainOperator operator, List<DomainType> operandTypes) {
        return new TypeErrorException(String.format("Cannot resolve operation type for operator %s and operand types %s", operator, operandTypes));
    }
}
