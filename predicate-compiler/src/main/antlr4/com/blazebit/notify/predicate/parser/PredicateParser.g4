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
    : left=arithmetic_expression 	comparison_operator 			right=arithmetic_expression		#ArithmeticComparisonPredicate
    | left=datetime_expression 		comparison_operator 			right=datetime_expression		#DateTimeComparisonPredicate
    | left=string_expression 		equality_comparison_operator 	right=string_expression			#StringComparisonPredicate
    | left=enum_expression 			equality_comparison_operator 	right=enum_expression			#EnumComparisonPredicate
    | between_expression                                                                            #BetweenPredicate
    | in_expression                                                                                 #InPredicate
    | null_expression                                                                               #NullPredicate
    ;

between_expression
	: left=datetime_expression BETWEEN lower=datetime_expression AND upper=datetime_expression			#DateTimeBetweenPredicate
	| left=arithmetic_expression BETWEEN lower=arithmetic_expression AND upper=arithmetic_expression	#ArithmeticBetweenPredicate
	;

in_expression
	: string_expression 	(not=NOT)? IN 			LP (in_items+=string_atom)? (COMMA in_items+=string_atom)* RP				#StringInPredicate
	| string_expression 	(not=NOT)? IN 			LP collection_attribute RP												    #StringInCollectionPredicate
	| arithmetic_expression (not=NOT)? IN 			LP (in_items+=arithmetic_in_item)? (COMMA in_items+=arithmetic_in_item)* RP	#ArithmeticInPredicate
	| arithmetic_expression (not=NOT)? IN 			LP collection_attribute RP												    #ArithmeticInCollectionPredicate
	| enum_expression 		(not=NOT)? IN 	        LP (in_items+=enum_atom)? (COMMA in_items+=enum_atom)* RP				    #EnumInPredicate
	| enum_expression       (not=NOT)? IN 			LP collection_attribute RP												    #EnumInCollectionPredicate
	;

null_expression
	: left=datetime_expression 		kind=(IS_NULL | IS_NOT_NULL)		#DateTimeIsNullPredicate
	| left=arithmetic_expression	kind=(IS_NULL | IS_NOT_NULL) 		#ArithmeticIsNullPredicate
	| left=string_expression		kind=(IS_NULL | IS_NOT_NULL) 		#StringIsNullPredicate
	| left=enum_expression			kind=(IS_NULL | IS_NOT_NULL) 		#EnumIsNullPredicate
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
    : sign=( OP_PLUS | OP_MINUS )? arithmetic_primary   #SimpleArithmeticPrimary
    ;

arithmetic_in_item
	: sign=( OP_PLUS | OP_MINUS )? arithmetic_atom  #ArithmeticInItem
	;

arithmetic_primary
    : arithmetic_atom               #ArithmeticAtom
    | LP arithmetic_expression RP   #ArithmeticPrimaryParanthesis
    ;

/****************************************************************
 * Date & Time
 ****************************************************************/

datetime_expression
	: datetime_atom
	;

datetime_atom
	: datetime_attribute
	| datetime_literal
    ;

datetime_attribute
    : identifier    #DatetimeAttribute
	;

datetime_literal
	: TIMESTAMP_LITERAL     #TimestampLiteral
    | CURRENT_TIMESTAMP     #CurrentTimestamp
	;

/****************************************************************
 * String
 ****************************************************************/

string_expression
	: string_atom
	;

string_atom
	: string_attribute
	| string_literal
	;

string_attribute
	: identifier    #StringAttribute
	;

string_literal
	: STRING_LITERAL    #StringLiteral
	;

/****************************************************************
 * Enum
 ****************************************************************/

enum_expression
	: enum_atom
	;

enum_atom
	: enum_attribute
	| enum_literal
	;

enum_attribute
	: identifier    #EnumAttribute
	;

enum_literal
	: ENUM_VALUE_FUNCTION LP enumName=STRING_LITERAL COMMA enumKey=STRING_LITERAL RP  #EnumLiteral
	;

/****************************************************************
 * Numeric
 ****************************************************************/

arithmetic_atom
    : numeric_attribute
    | numeric_literal
    ;

numeric_attribute
    : identifier    #NumericAttribute
	;

numeric_literal
    : NUMERIC_LITERAL   #NumericLiteral
	;

/****************************************************************
 * Collection
 ****************************************************************/

collection_attribute
	: identifier    #CollectionAttribute
	;

identifier
    : IDENTIFIER
    ;