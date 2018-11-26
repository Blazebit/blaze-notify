/*
 * Copyright 2014 Blazebit.
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
parser grammar PredicateParser;

options { tokenVocab=PredicateLexer; }

start
    : conditional_expression EOF;

conditional_expression
    : term=conditional_term							 		#TermPredicate
    | left=conditional_expression OR term=conditional_term 	#OrPredicate
    ;

conditional_term
    : factor=conditional_factor							  #FactorPredicate
    | left=conditional_term AND factor=conditional_factor #AndPredicate
    ;

conditional_factor
    : (not=NOT)? expr=conditional_primary;

conditional_primary
    : expr=comparison_expression	    #SimplePredicate
    | LP expr=conditional_expression RP #NestedPredicate
    ;

comparison_expression
    : left=arithmetic_expression 	comparison_operator 			right=arithmetic_expression		                                        #ComparisonPredicate
    | left=arithmetic_expression BETWEEN lower=arithmetic_expression AND upper=arithmetic_expression                                        #BetweenPredicate
    | arithmetic_expression 	(not=NOT)? IN 			LP (in_items+=arithmetic_in_item)? (COMMA in_items+=arithmetic_in_item)* RP         #InPredicate
	| arithmetic_expression 	(not=NOT)? IN 			enum_literal_or_path			                                                                #InCollectionPredicate
    | left=arithmetic_expression		kind=(IS_NULL | IS_NOT_NULL)                                                                        #IsNullPredicate
    ;

equality_comparison_operator
    : OP_EQ
    | OP_NEQ1
    | OP_NEQ2
    ;

comparison_operator
    : equality_comparison_operator
    | OP_GT
    | OP_GE
    | OP_LT
    | OP_LE
    ;

arithmetic_expression
    : arithmetic_term												    #SimpleArithmeticTerm
    | arithmetic_expression op=( OP_PLUS | OP_MINUS ) arithmetic_term	#AdditiveExpression
    ;

arithmetic_term
    : arithmetic_factor											    #SimpleArithmeticFactor
    | arithmetic_term op=( OP_MUL | OP_DIV ) arithmetic_factor	    #MultiplicativeExpression
    ;

arithmetic_factor
    : sign=( OP_PLUS | OP_MINUS )? arithmetic_primary   #ArithmeticPrimary
    ;

arithmetic_in_item
	: sign=( OP_PLUS | OP_MINUS )? atom  #ArithmeticInItem
	;

arithmetic_primary
    : atom               #ArithmeticAtom
    | LP arithmetic_expression RP   #ArithmeticPrimaryParanthesis
    ;

atom
    : enum_literal_or_path
    | entity_literal_or_function_invocation
    | datetime_literal
    | temporal_interval_literal
    | numeric_literal
    | string_literal
    | collection_literal
    ;

enum_literal_or_path
    : pathRoot=identifier (DOT pathElements+=identifier)+
    ;

entity_literal_or_function_invocation
    : name=identifier                                                                                                                                                                                   #RootPathOrNoArgFunctionInvocation
    | name=identifier LP ((args+=conditional_expression|arithmetic_expression) COMMA)* RP                                                                                                               #IndexedFunctionInvocation
    | name=identifier LP (argNames+=identifier OP_EQ args+=conditional_expression|arithmetic_expression) (COMMA (argNames+=identifier OP_EQ args+=conditional_expression|arithmetic_expression))* RP    #NamedInvocation
    ;

datetime_literal
	: TIMESTAMP LP content=TIMESTAMP_LITERAL_CONTENT RP    #TimestampLiteral
	;

temporal_interval_literal
    : INTERVAL content=TEMPORAL_INTERVAL_LITERAL_CONTENT #TemporalIntervalLiteral
    ;

string_literal
	: STRING_LITERAL    #StringLiteral
	;

numeric_literal
    : NUMERIC_LITERAL   #NumericLiteral
	;

collection_literal
    : LB ((values+=atom) (COMMA (values+=atom))*)? RB   #CollectionLiteral
	;

identifier
    : IDENTIFIER
    ;