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

import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.expression.Expression;
import com.blazebit.notify.expression.ExpressionCompiler;
import com.blazebit.notify.expression.Predicate;
import com.blazebit.notify.expression.SyntaxErrorException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.BitSet;
import java.util.Map;

public class ExpressionCompilerImpl implements ExpressionCompiler {

    private static final RuleInvoker<Predicate> PREDICATE_RULE_INVOKER = new RuleInvoker<Predicate>() {
        @Override
        public ParserRuleContext invokeRule(PredicateParser parser) {
            return parser.start();
        }
    };
    private static final RuleInvoker<Expression> EXPRESSION_RULE_INVOKER = new RuleInvoker<Expression>() {
        @Override
        public ParserRuleContext invokeRule(PredicateParser parser) {
            return parser.arithmetic_expression();
        }
    };

    private final DomainModel domainModel;
    private final LiteralFactory literalFactory;

    public ExpressionCompilerImpl(DomainModel domainModel, LiteralFactory literalFactory) {
        this.domainModel = domainModel;
        this.literalFactory = literalFactory;
    }

    @Override
    public Context createContext(Map<String, DomainType> rootDomainTypes) {
        return new CompileContext(rootDomainTypes);
    }

    @Override
    public Expression createExpression(String expressionString, Context compileContext) {
        return parse(expressionString, EXPRESSION_RULE_INVOKER, compileContext);
    }

    @Override
    public Predicate createPredicate(String expressionString, Context compileContext) {
        return parse(expressionString, PREDICATE_RULE_INVOKER, compileContext);
    }

    public LiteralFactory getLiteralFactory() {
        return literalFactory;
    }

    @SuppressWarnings("unchecked")
    <T extends Expression> T parse(String input, RuleInvoker<T> ruleInvoker, Context compileContext) {
        SimpleErrorListener errorListener = new SimpleErrorListener();
        PredicateLexer lexer = new PredicateLexer(CharStreams.fromString(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PredicateParser parser = new PredicateParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParserRuleContext ctx = ruleInvoker.invokeRule(parser);
        if (input.length() != ctx.getStop().getStopIndex() + 1) {
            throw new SyntaxErrorException("Parsing stopped at index " + ctx.getStop().getStopIndex() + "! Illegal unexpected suffix: '" + input.substring(ctx.getStop().getStopIndex() + 1) + "'");
        }

        PredicateModelGenerator visitor = new PredicateModelGenerator(domainModel, literalFactory, compileContext);
        return (T) visitor.visit(ctx);
    }

    public interface RuleInvoker<T extends Expression> {
        ParserRuleContext invokeRule(PredicateParser parser);
    }

    protected static final ANTLRErrorListener ERR_LISTENER = new ANTLRErrorListener() {

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new SyntaxErrorException("line " + line + ":" + charPositionInLine + " " + msg);
        }

        @Override
        public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        }

        @Override
        public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        }

        @Override
        public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        }
    };

    private static class SimpleErrorListener implements ANTLRErrorListener {

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            String reason;

            if (e instanceof LexerNoViableAltException) {
                reason = "Unexpected input at '" + (char) e.getInputStream().LA(1) + "'.";
            } else if (offendingSymbol instanceof CommonToken) {
                reason = "Unexpected input at '" + ((CommonToken) offendingSymbol).getText() + "'.";
                IntervalSet expectedToken = recognizer.getATN().getExpectedTokens(recognizer.getState(), ((Parser) recognizer).getContext());

                if (expectedToken.size() > 0) {
                    reason += " One of the following characters is expected ";
                    reason += expectedToken.toString(PredicateLexer.VOCABULARY);
                }
            } else {
                reason = msg;
            }
            throw new SyntaxErrorException("Unexpected input at line " + line + ", col " + charPositionInLine + ". " + reason);
        }

        @Override
        public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        }

        @Override
        public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        }

        @Override
        public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        }
    }

    private static class CompileContext implements ExpressionCompiler.Context {

        private final Map<String, DomainType> rootDomainTypes;

        public CompileContext(Map<String, DomainType> rootDomainTypes) {
            this.rootDomainTypes = rootDomainTypes;
        }

        @Override
        public DomainType getRootDomainType(String alias) {
            return rootDomainTypes.get(alias);
        }
    }
}
