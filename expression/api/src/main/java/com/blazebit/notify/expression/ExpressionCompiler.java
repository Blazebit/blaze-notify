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

package com.blazebit.notify.expression;

import com.blazebit.notify.domain.runtime.model.DomainType;

import java.util.Collections;
import java.util.Map;

public interface ExpressionCompiler {

    public Context createContext(Map<String, DomainType> rootDomainTypes);

    public default Expression createExpression(String expressionString) {
        return createExpression(expressionString, createContext(Collections.emptyMap()));
    }

    public Expression createExpression(String expressionString, Context compileContext);

    public default Predicate createPredicate(String expressionString) {
        return createPredicate(expressionString, createContext(Collections.emptyMap()));
    }

    public Predicate createPredicate(String expressionString, Context compileContext);

    public interface Context {

        public DomainType getRootDomainType(String alias);

    }
}
