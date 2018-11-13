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
import com.blazebit.notify.expression.Predicate;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.BitSet;

public class PredicateCompiler {

    private final DomainModel domainModel;
    private final LiteralFactory literalFactory;

    private static final RuleInvoker predicateRuleInvoker = new RuleInvoker() {
        @Override
        public ParserRuleContext invokeRule(PredicateParser parser) {
            return parser.start();
        }
    };

    public PredicateCompiler(DomainModel domainModel) {
        this.domainModel = domainModel;
        this.literalFactory = new LiteralFactory(domainModel);
    }

    public LiteralFactory getLiteralFactory() {
        return literalFactory;
    }

    public Predicate parsePredicate(String input) {
        return parse(input, predicateRuleInvoker);
    }

    Predicate parse(String input, RuleInvoker ruleInvoker) {
        SimpleErrorListener errorListener = new SimpleErrorListener();
        PredicateLexer lexer = new PredicateLexer(CharStreams.fromString(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PredicateParser parser = new PredicateParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParserRuleContext ctx = ruleInvoker.invokeRule(parser);

        PredicateModelGenerator visitor = new PredicateModelGenerator(domainModel, literalFactory);
        return (Predicate) visitor.visit(ctx);
    }

    public interface RuleInvoker {
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
}
