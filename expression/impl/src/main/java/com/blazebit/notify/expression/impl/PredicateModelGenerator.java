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
    private Literal cachedBooleanTrueLiteral;
    private Literal cachedBooleanFalseLiteral;

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
        Predicate leftTerm = (Predicate) ctx.left.accept(this);
        Predicate rightTerm = (Predicate) ctx.term.accept(this);

        CompoundPredicate disjunctivePredicate;
        if (leftTerm instanceof CompoundPredicate && !((CompoundPredicate) leftTerm).isConjunction() && !leftTerm.isNegated()) {
            disjunctivePredicate = (CompoundPredicate) leftTerm;
            disjunctivePredicate.getPredicates().add(rightTerm);
        } else {
            disjunctivePredicate = new CompoundPredicate(getBooleanDomainType(), new ArrayList<>(2), false);
            disjunctivePredicate.getPredicates().add(leftTerm);
            disjunctivePredicate.getPredicates().add(rightTerm);
        }
        return disjunctivePredicate;
    }

    @Override
    public Predicate visitAndPredicate(PredicateParser.AndPredicateContext ctx) {
        Predicate leftTerm = (Predicate) ctx.left.accept(this);
        Predicate rightFactor = (Predicate) ctx.factor.accept(this);

        CompoundPredicate conjunctivePredicate;
        if (leftTerm instanceof CompoundPredicate && ((CompoundPredicate) leftTerm).isConjunction() && !leftTerm.isNegated()) {
            conjunctivePredicate = (CompoundPredicate) leftTerm;
            conjunctivePredicate.getPredicates().add(rightFactor);
        } else {
            conjunctivePredicate = new CompoundPredicate(getBooleanDomainType(), new ArrayList<>(2), true);
            conjunctivePredicate.getPredicates().add(leftTerm);
            conjunctivePredicate.getPredicates().add(rightFactor);
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

        List<DomainType> operandTypes = Arrays.asList(left.getType(), right.getType());
        DomainPredicateTypeResolver predicateTypeResolver = domainModel.getPredicateTypeResolver(left.getType().getName(), comparisonOperator.getDomainPredicateType());

        if (predicateTypeResolver == null) {
            throw missingPredicateTypeResolver(left.getType(), comparisonOperator.getDomainPredicateType());
        } else {
            DomainType domainType = predicateTypeResolver.resolveType(domainModel, operandTypes);
            if (domainType == null) {
                throw cannotResolvePredicateType(comparisonOperator.getDomainPredicateType(), operandTypes);
            } else {
                return new ComparisonPredicate(domainType, left, right, comparisonOperator);
            }
        }
    }

    @Override
    public Expression visitInPredicate(PredicateParser.InPredicateContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.arithmetic_expression().accept(this);
        List<ArithmeticExpression> inItems = getLiteralList(ArithmeticExpression.class, ctx.in_items);

        DomainPredicateTypeResolver predicateTypeResolver = domainModel.getPredicateTypeResolver(left.getType().getName(), DomainPredicateType.EQUALITY);

        if (predicateTypeResolver == null) {
            throw missingPredicateTypeResolver(left.getType(), DomainPredicateType.EQUALITY);
        } else {
            List<DomainType> operandTypes = new ArrayList<>(inItems.size() + 1);
            operandTypes.add(left.getType());
            for (int i = 0; i < inItems.size(); i++) {
                ArithmeticExpression inItem = inItems.get(i);
                operandTypes.add(inItem.getType());
            }
            DomainType domainType = predicateTypeResolver.resolveType(domainModel, operandTypes);
            if (domainType == null) {
                throw cannotResolvePredicateType(DomainPredicateType.EQUALITY, operandTypes);
            } else {
                return new InPredicate(domainType, left, inItems, ctx.not != null);
            }
        }
    }

    @Override
    public Predicate visitBetweenPredicate(PredicateParser.BetweenPredicateContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.left.accept(this);
        ArithmeticExpression lower = (ArithmeticExpression) ctx.lower.accept(this);
        ArithmeticExpression upper = (ArithmeticExpression) ctx.upper.accept(this);

        DomainPredicateTypeResolver predicateTypeResolver = domainModel.getPredicateTypeResolver(left.getType().getName(), DomainPredicateType.RELATIONAL);

        if (predicateTypeResolver == null) {
            throw missingPredicateTypeResolver(left.getType(), DomainPredicateType.RELATIONAL);
        } else {
            List<DomainType> operandTypes = Arrays.asList(left.getType(), lower.getType(), upper.getType());
            DomainType domainType = predicateTypeResolver.resolveType(domainModel, operandTypes);
            if (domainType == null) {
                throw cannotResolvePredicateType(DomainPredicateType.RELATIONAL, operandTypes);
            } else {
                return new BetweenPredicate(domainType, left, upper, lower);
            }
        }
    }

//    @Override
//    public Predicate visitStringInCollectionPredicate(PredicateParser.StringInCollectionPredicateContext ctx) {
//        return new StringInCollectionPredicate((StringAtom) ctx.string_expression().accept(this), (CollectionAtom) ctx.collection_attribute().accept(this), ctx.not != null);
//    }

    @Override
    public Predicate visitIsNullPredicate(PredicateParser.IsNullPredicateContext ctx) {
        ArithmeticExpression left = (ArithmeticExpression) ctx.left.accept(this);

        DomainPredicateTypeResolver predicateTypeResolver = domainModel.getPredicateTypeResolver(left.getType().getName(), DomainPredicateType.NULLNESS);

        if (predicateTypeResolver == null) {
            throw missingPredicateTypeResolver(left.getType(), DomainPredicateType.NULLNESS);
        } else {
            List<DomainType> operandTypes = Collections.singletonList(left.getType());
            DomainType domainType = predicateTypeResolver.resolveType(domainModel, operandTypes);
            if (domainType == null) {
                throw cannotResolvePredicateType(DomainPredicateType.NULLNESS, operandTypes);
            } else {
                return new IsNullPredicate(domainType, left, ctx.kind.getType() == PredicateParser.IS_NOT_NULL);
            }
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
            throw missingOperationTypeResolver(left.getType(), operator.getDomainOperator());
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
        Expression accept = ctx.arithmetic_primary().accept(this);
        if (ctx.sign == null) {
            return accept;
        }
        return new ArithmeticFactor((ArithmeticExpression) accept, ctx.sign.getType() == PredicateParser.OP_MINUS);
    }

    @Override
    public Expression visitArithmeticInItem(PredicateParser.ArithmeticInItemContext ctx) {
        ArithmeticExpression atom = (ArithmeticExpression) ctx.atom().accept(this);

        if (ctx.sign == null || ctx.sign.getType() == PredicateParser.OP_PLUS) {
            return atom;
        } else {
            if (atom.getType().getEnabledOperators().contains(DomainOperator.UNARY_MINUS)) {
                return new ArithmeticFactor(atom, true);
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
        return new Literal(literalFactory.ofDateTimeString(ctx.content.getText()));
    }

    @Override
    public Expression visitTemporalIntervalLiteral(PredicateParser.TemporalIntervalLiteralContext ctx) {
        return new Literal(literalFactory.ofTemporalIntervalString(ctx.content.getText()));
    }

    @Override
    public Expression visitStringLiteral(PredicateParser.StringLiteralContext ctx) {
        return new Literal(literalFactory.ofQuotedString(ctx.STRING_LITERAL().getText()));
    }

    @Override
    public Expression visitNumericLiteral(PredicateParser.NumericLiteralContext ctx) {
        return new Literal(literalFactory.ofNumericString(ctx.NUMERIC_LITERAL().getText()));
    }

    @Override
    public Expression visitBooleanLiteral(PredicateParser.BooleanLiteralContext ctx) {
        char c = ctx.BOOLEAN_LITERAL().getText().charAt(0);
        boolean value = c == 't' || c == 'T';
        return getBooleanLiteral(value);
    }

    @Override
    public Expression visitCollectionLiteral(PredicateParser.CollectionLiteralContext ctx) {
        List<Expression> literalList = getLiteralList(Expression.class, ctx.values);
        CollectionDomainType collectionDomainType;
        if (literalList.isEmpty()) {
            collectionDomainType = domainModel.getCollectionType(null);
        } else {
            collectionDomainType = domainModel.getCollectionType(literalList.get(0).getType());
        }

        return new Literal(literalFactory.ofCollectionValues(collectionDomainType, literalList));
    }

    @Override
    public Expression visitEnum_literal_or_path(PredicateParser.Enum_literal_or_pathContext ctx) {
        String alias = ctx.pathRoot.getText();
        DomainType type = compileContext.getRootDomainType(alias);
        if (type == null) {
            if (ctx.pathElements.size() == 1) {
                type = domainModel.getType(alias);
                if (type instanceof EnumDomainType) {
                    return new Literal(literalFactory.ofEnumValue((EnumDomainType) type, ctx.pathElements.get(0).getText()));
                }
            }
            throw unknownType(alias);
        } else {
            List<EntityDomainTypeAttribute> pathAttributes = new ArrayList<>(ctx.pathElements.size());
            for (int pathElemIdx = 0; pathElemIdx < ctx.pathElements.size(); pathElemIdx++) {
                String pathElement = ctx.pathElements.get(pathElemIdx).getText();
                if (type instanceof CollectionDomainType) {
                    type = ((CollectionDomainType) type).getElementType();
                }
                if (type instanceof EntityDomainType) {
                    EntityDomainType entityType = ((EntityDomainType) type);
                    EntityDomainTypeAttribute attribute = entityType.getAttribute(pathElement);
                    pathAttributes.add(attribute);
                    if (attribute == null) {
                        throw unknownEntityAttribute(entityType, pathElement);
                    } else {
                        type = attribute.getType();
                    }
                } else {
                    throw unsupportedType(type.toString());
                }
            }

            return new Path(alias, pathAttributes, type);
        }
    }

    @Override
    public Expression visitBooleanLiteralPredicate(PredicateParser.BooleanLiteralPredicateContext ctx) {
        Literal literal = (Literal) ctx.boolean_literal().accept(this);
        return new ComparisonPredicate(getBooleanDomainType(), literal, getBooleanTrueLiteral(), ComparisonOperator.EQUAL);
    }

    @Override
    public Expression visitBooleanFunction(PredicateParser.BooleanFunctionContext ctx) {
        Expression expression = super.visitBooleanFunction(ctx);
        if (expression.getType() == getBooleanDomainType()) {
            return expression;
        }

        throw new TypeErrorException("Invalid use of non-boolean returning function: " + ctx.getText());
    }

    @Override
    public Expression visitRootPathOrNoArgFunctionInvocation(PredicateParser.RootPathOrNoArgFunctionInvocationContext ctx) {
        String aliasOrFunctionName = ctx.name.getText();
        DomainFunction function = domainModel.getFunction(aliasOrFunctionName);
        if (function == null) {
            DomainType domainType = compileContext.getRootDomainType(aliasOrFunctionName);
            if (domainType == null) {
                throw unknownFunction(aliasOrFunctionName);
            }
            return new Path(aliasOrFunctionName, Path.empty(), domainType);
        } else {
            DomainFunctionTypeResolver functionTypeResolver = domainModel.getFunctionTypeResolver(aliasOrFunctionName);
            DomainType functionType = functionTypeResolver.resolveType(domainModel, function, Collections.emptyMap());
            return new FunctionInvocation(function, Collections.emptyMap(), functionType);
        }
    }

    @Override
    public Expression visitIndexedFunctionInvocation(PredicateParser.IndexedFunctionInvocationContext ctx) {
        String functionName = ctx.name.getText();
        DomainFunction function = domainModel.getFunction(functionName);
        if (function == null) {
            throw unknownFunction(functionName);
        } else {
            List<Expression> literalList = getLiteralList(Expression.class, ctx.args);
            if (literalList.size() > function.getArgumentCount()) {
                throw new DomainModelException(String.format("Function '%s' expects at most %d arguments but found %d",
                        function.getName(),
                        function.getArgumentCount(),
                        literalList.size()
                ));
            }
            Map<DomainFunctionArgument, Expression> arguments = new LinkedHashMap<>(literalList.size());
            Map<DomainFunctionArgument, DomainType> argumentTypes = new HashMap<>(literalList.size());
            for (int i = 0; i < literalList.size(); i++) {
                DomainFunctionArgument domainFunctionArgument = function.getArguments().get(i);
                argumentTypes.put(domainFunctionArgument, literalList.get(i).getType());
                arguments.put(domainFunctionArgument, literalList.get(i));
            }
            DomainFunctionTypeResolver functionTypeResolver = domainModel.getFunctionTypeResolver(functionName);
            DomainType functionType = functionTypeResolver.resolveType(domainModel, function, argumentTypes);
            return new FunctionInvocation(function, arguments, functionType);
        }
    }

    @Override
    public Expression visitNamedInvocation(PredicateParser.NamedInvocationContext ctx) {
        String entityOrFunctionName = ctx.name.getText();
        DomainFunction function = domainModel.getFunction(entityOrFunctionName);
        if (function == null) {
            DomainType type = domainModel.getType(entityOrFunctionName);
            if (type instanceof EntityDomainType) {
                EntityDomainType entityDomainType = (EntityDomainType) type;
                List<PredicateParser.IdentifierContext> argNames = ctx.argNames;
                List<Expression> literalList = getLiteralList(Expression.class, ctx.args);
                Map<EntityDomainTypeAttribute, Expression> arguments = new LinkedHashMap<>(literalList.size());
                for (int i = 0; i < literalList.size(); i++) {
                    EntityDomainTypeAttribute attribute = entityDomainType.getAttribute(argNames.get(i).getText());
                    arguments.put(attribute, literalList.get(i));
                }
                return new Literal(literalFactory.ofEntityAttributeValues(entityDomainType, arguments));
            } else {
                throw unknownFunction(entityOrFunctionName);
            }
        } else {
            List<PredicateParser.IdentifierContext> argNames = ctx.argNames;
            List<Expression> literalList = getLiteralList(Expression.class, ctx.args);
            Map<DomainFunctionArgument, Expression> arguments = new LinkedHashMap<>(literalList.size());
            if (arguments.size() > function.getArgumentCount()) {
                throw new DomainModelException(String.format("Function '%s' expects at most %d arguments but found %d",
                        function.getName(),
                        function.getArgumentCount(),
                        arguments.size()
                ));
            }
            Map<DomainFunctionArgument, DomainType> argumentTypes = new HashMap<>(arguments.size());
            for (int i = 0; i < literalList.size(); i++) {
                DomainFunctionArgument domainFunctionArgument = function.getArgument(argNames.get(i).getText());
                argumentTypes.put(domainFunctionArgument, literalList.get(i).getType());
                arguments.put(domainFunctionArgument, literalList.get(i));
            }
            DomainFunctionTypeResolver functionTypeResolver = domainModel.getFunctionTypeResolver(entityOrFunctionName);
            DomainType functionType = functionTypeResolver.resolveType(domainModel, function, argumentTypes);
            return new FunctionInvocation(function, arguments, functionType);
        }
    }
//    @Override
//    public Expression visitCollectionAttribute(PredicateParser.CollectionAttributeContext ctx) {
//        return new CollectionAtom(new Path(ctx.identifier().getText(), TermType.COLLECTION));
//    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getLiteralList(Class<T> clazz, List<? extends ParserRuleContext> items) {
        List<T> literals = new ArrayList<>();
        for (ParserRuleContext item : items) {
            literals.add((T) item.accept(this));
        }
        return literals;
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

    private Literal getBooleanLiteral(boolean value) {
        return value ? getBooleanTrueLiteral() : getBooleanFalseLiteral();
    }

    private Literal getBooleanTrueLiteral() {
        if (cachedBooleanTrueLiteral == null) {
            cachedBooleanTrueLiteral = new Literal(literalFactory.ofBoolean(true));
        }
        return cachedBooleanTrueLiteral;
    }

    private Literal getBooleanFalseLiteral() {
        if (cachedBooleanFalseLiteral == null) {
            cachedBooleanFalseLiteral = new Literal(literalFactory.ofBoolean(false));
        }
        return cachedBooleanFalseLiteral;
    }

    private TypeErrorException typeError(DomainType t1, DomainType t2, DomainOperator operator) {
        return new TypeErrorException(String.format("%s %s %s", t1, operator, t2));
    }

    private DomainModelException missingPredicateTypeResolver(DomainType type, DomainPredicateType predicateType) {
        return new DomainModelException(String.format("Missing predicate type resolver for type %s and predicate %s", type, predicateType));
    }

    private DomainModelException missingOperationTypeResolver(DomainType type, DomainOperator operator) {
        return new DomainModelException(String.format("Missing operation type resolver for type %s and operator %s", type, operator));
    }

    private TypeErrorException typeError(DomainType t1, DomainType t2, DomainPredicateType predicateType) {
        return new TypeErrorException(String.format("%s %s %s", t1, predicateType, t2));
    }

    private DomainModelException unknownType(String typeName) {
        return new DomainModelException(String.format("Undefined type '%s'", typeName));
    }

    private DomainModelException unknownEntityAttribute(EntityDomainType entityDomainType, String attributeName) {
        return new DomainModelException(String.format("Attribute %s undefined for entity %s", attributeName, entityDomainType));
    }

    private DomainModelException unknownFunction(String identifier) {
        return new DomainModelException(String.format("Undefined function '%s'", identifier));
    }

    private TypeErrorException unsupportedType(String typeName) {
        return new TypeErrorException(String.format("Resolved type for identifier %s is not supported", typeName));
    }

    private TypeErrorException cannotResolvePredicateType(DomainPredicateType predicateType, List<DomainType> operandTypes) {
        return new TypeErrorException(String.format("Cannot resolve predicate type for predicate %s and operand types %s", predicateType, operandTypes));
    }

    private TypeErrorException cannotResolveOperationType(DomainOperator operator, List<DomainType> operandTypes) {
        return new TypeErrorException(String.format("Cannot resolve operation type for operator %s and operand types %s", operator, operandTypes));
    }
}
