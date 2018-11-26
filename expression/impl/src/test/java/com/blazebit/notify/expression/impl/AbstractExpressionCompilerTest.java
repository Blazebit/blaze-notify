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

import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.impl.boot.model.DomainBuilderImpl;
import com.blazebit.notify.domain.runtime.model.*;
import com.blazebit.notify.expression.*;
import com.blazebit.notify.expression.impl.model.Gender;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.Before;
import org.junit.BeforeClass;

import java.math.BigDecimal;
import java.util.*;

public abstract class AbstractExpressionCompilerTest {

    private ExpressionCompilerImpl expressionCompiler;
    private static DomainModel defaultDomainModel;
    private DomainModel domainModel;

    @BeforeClass
    public static void createDefaultTestDomainModel() {
        DomainBuilder builder = new DomainBuilderImpl()
                .createBasicType("boolean", Boolean.class)
                .withOperator("boolean", new DomainOperator[]{ DomainOperator.NOT })
                .withPredicate("boolean", DomainPredicateType.distinguishable())
                .createBasicType("long", Long.class)
                .withOperator("long", DomainOperator.arithmetic())
                .withPredicate("long", DomainPredicateType.comparable())
                .createBasicType("integer", Integer.class)
                .withOperator("integer", DomainOperator.arithmetic())
                .withPredicate("integer", DomainPredicateType.comparable())
                .createBasicType("bigdecimal", BigDecimal.class)
                .withOperator("bigdecimal", DomainOperator.arithmetic())
                .withPredicate("bigdecimal", DomainPredicateType.comparable())
                .createBasicType("string", String.class)
                .withOperator("string", new DomainOperator[]{ DomainOperator.PLUS })
                .withPredicate("string", DomainPredicateType.distinguishable())
                .createBasicType("timestamp", Calendar.class)
                .withPredicate("timestamp", DomainPredicateType.comparable())
                .createEnumType("gender", Gender.class)
                    .withValue(Gender.FEMALE.name())
                    .withValue(Gender.MALE.name())
                .build()
                .withPredicate("gender", DomainPredicateType.distinguishable())
                .withLiteralTypeResolver(new DefaultNumericLiteralTypeResolver())
                .withLiteralTypeResolver(new DefaultStringLiteralTypeResolver())
                .withLiteralTypeResolver(new DefaultTemporalLiteralTypeResolver())
                .withLiteralTypeResolver(new DefaultEnumLiteralTypeResolver())
                .createEntityType("user")
                    .addAttribute("id", Long.class)
                    .addAttribute("email", String.class)
                    .addAttribute("age", Integer.class)
                    .addAttribute("birthday", Calendar.class)
                    .addAttribute("gender", Gender.class)
                .build();

        for (final Class<?> type : Arrays.asList(Integer.class, Long.class, BigDecimal.class)) {
            builder.withOperationTypeResolver(type, DomainOperator.MODULO, StaticDomainOperationTypeResolvers.returning(Integer.class));
            builder.withOperationTypeResolver(type, DomainOperator.UNARY_MINUS, StaticDomainOperationTypeResolvers.returning(type));
            builder.withOperationTypeResolver(type, DomainOperator.UNARY_PLUS, StaticDomainOperationTypeResolvers.returning(type));
            builder.withOperationTypeResolver(type, DomainOperator.DIVISION, StaticDomainOperationTypeResolvers.returning(BigDecimal.class));
            for (DomainOperator domainOperator : Arrays.asList(DomainOperator.PLUS, DomainOperator.MINUS, DomainOperator.MULTIPLICATION)) {
                builder.withOperationTypeResolver(type, domainOperator, StaticDomainOperationTypeResolvers.widest(BigDecimal.class, Integer.class));
            }
        }

        defaultDomainModel = builder.build();
    }

    @Before
    public void setup() {
        domainModel = createDomainModel();
        expressionCompiler = new ExpressionCompilerImpl(domainModel);
    }

    protected DomainModel createDomainModel() {
        return defaultDomainModel;
    }

    protected ExpressionCompiler.Context getCompileContext() {
        return expressionCompiler.createContext(Collections.<String, DomainType>emptyMap());
    }

    protected Predicate parsePredicate(String input) {
        return expressionCompiler.createPredicate(input, getCompileContext());
    }

    protected Expression parseArithmeticExpression(String input) {
        return expressionCompiler.parse(input, new ExpressionCompilerImpl.RuleInvoker() {
            @Override
            public ParserRuleContext invokeRule(PredicateParser parser) {
                return parser.arithmetic_expression();
            }
        }, getCompileContext());
    }

    protected DisjunctivePredicate or(Predicate... disjuncts) {
        return new DisjunctivePredicate(booleanDomainType(), Arrays.asList(disjuncts));
    }

    protected ConjunctivePredicate and(Predicate... conjuncts) {
        return new ConjunctivePredicate(booleanDomainType(), Arrays.asList(conjuncts));
    }

    protected Atom time(Calendar value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofCalendar(value));
    }

    protected Atom time(String value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofDateTimeString(value));
    }

    protected Atom interval(String value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofTemporalIntervalString(value));
    }

    protected static String wrapTimestamp(String dateTimeStr) {
        return "TIMESTAMP('" + dateTimeStr + "')";
    }

    protected Atom string(String value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofString(value));
    }

    protected Atom number(long value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofBigDecimal(new BigDecimal(value)));
    }

    protected Atom number(BigDecimal value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofBigDecimal(value));
    }

    protected Atom number(String value) {
        return new Atom(expressionCompiler.getLiteralFactory().ofNumericString(value));
    }

    protected Atom attr(String entity, String attribute) {
        DomainType type = ((EntityDomainType) domainModel.getType(entity)).getAttribute(attribute).getType();
        return new Atom(new Attribute(entity, attribute, type));
    }

    protected Atom enumValue(String enumName, String enumKey) {
        return new Atom(expressionCompiler.getLiteralFactory().ofEnumValue(new EnumValue(enumName, enumKey)));
    }

//	protected static CollectionAtom collectionAttr(String identifier) {
//		return new CollectionAtom(new Attribute(identifier, TermType.COLLECTION));
//	}

    protected ComparisonPredicate neq(ArithmeticExpression left, ArithmeticExpression right) {
        return new ComparisonPredicate(booleanDomainType(), left, right, ComparisonOperator.NOT_EQUAL);
    }

    protected ComparisonPredicate eq(ArithmeticExpression left, ArithmeticExpression right) {
        return new ComparisonPredicate(booleanDomainType(), left, right, ComparisonOperator.EQUAL);
    }

    protected ComparisonPredicate gt(ArithmeticExpression left, ArithmeticExpression right) {
        return new ComparisonPredicate(booleanDomainType(), left, right, ComparisonOperator.GREATER);
    }

    protected ComparisonPredicate ge(ArithmeticExpression left, ArithmeticExpression right) {
        return new ComparisonPredicate(booleanDomainType(), left, right, ComparisonOperator.GREATER_OR_EQUAL);
    }

    protected ComparisonPredicate lt(ArithmeticExpression left, ArithmeticExpression right) {
        return new ComparisonPredicate(booleanDomainType(), left, right, ComparisonOperator.LOWER);
    }

    protected ComparisonPredicate le(ArithmeticExpression left, ArithmeticExpression right) {
        return new ComparisonPredicate(booleanDomainType(), left, right, ComparisonOperator.LOWER_OR_EQUAL);
    }

    protected static ArithmeticFactor pos(ArithmeticExpression expression) {
        return new ArithmeticFactor(expression, false);
    }

    protected static ArithmeticFactor neg(ArithmeticExpression expression) {
        return new ArithmeticFactor(expression, true);
    }

    protected static ArithmeticExpression plus(ArithmeticExpression left, ArithmeticExpression right) {
        return new ChainingArithmeticExpression(left, right, ArithmeticOperatorType.PLUS);
    }

    protected static ArithmeticExpression minus(ArithmeticExpression left, ArithmeticExpression right) {
        return new ChainingArithmeticExpression(left, right, ArithmeticOperatorType.MINUS);
    }

    protected BetweenPredicate between(ArithmeticExpression left, ArithmeticExpression lower, ArithmeticExpression upper) {
        return new BetweenPredicate(booleanDomainType(), left, upper, lower);
    }

//	protected static StringInCollectionPredicate inCollection(StringAtom value, CollectionAtom collection) {
//		return new StringInCollectionPredicate(value, collection, false);
//	}

    protected InPredicate in(ArithmeticExpression value, ArithmeticExpression... items) {
        return new InPredicate(booleanDomainType(), value, Arrays.asList(items), false);
    }

    protected Atom functionInvocation(String functionName, Expression... arguments) {
        DomainFunction domainFunction = domainModel.getFunction(functionName);
        Map<DomainFunctionArgument, DomainType> argumentTypes = new HashMap<>();
        for (int i = 0; i < arguments.length; i++) {
            argumentTypes.put(domainFunction.getArguments().get(i), arguments[i].getType());
        }
        DomainType functionType = domainModel.getFunctionTypeResolver(functionName).resolveType(domainModel, domainFunction, argumentTypes);
        return new Atom(new FunctionInvocation(functionName, Arrays.asList(arguments), functionType));
    }

    private DomainType booleanDomainType() {
        return domainModel.getType(Boolean.class);
    }

    interface ExpectedExpressionProducer<T extends AbstractExpressionCompilerTest> {
        Expression getExpectedExpression(T testInstance);
    }
}
