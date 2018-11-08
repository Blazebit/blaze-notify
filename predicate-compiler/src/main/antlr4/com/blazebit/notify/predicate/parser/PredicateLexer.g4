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
lexer grammar PredicateLexer;

BETWEEN: B E T W E E N;
AND : A N D;
OR : O R;
NOT : N O T | '!';
IN: I N;
MEMBER_OF: M E M B E R ' ' O F;
IS_NULL: I S ' ' N U L L;
IS_NOT_NULL: I S ' ' N O T ' ' N U L L;

CURRENT_TIMESTAMP : C U R R E N T '_' TIMESTAMP;

// Literal

TIMESTAMP_LITERAL
    : TIMESTAMP '(' '\'' DATE_STRING (' ' TIME_STRING)? '\'' ')';

fragment TIMESTAMP
    : T I M E S T A M P;

DATE_STRING
    : DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT;

TIME_STRING
    : DIGIT DIGIT ':' DIGIT DIGIT ':' DIGIT DIGIT ('.' DIGIT DIGIT DIGIT)?;
    
fragment DIGIT
    : '0'..'9';
fragment DIGITS
    : DIGIT+;
fragment DIGIT_NOT_ZERO
    : '1'..'9';

STRING_LITERAL
	: '\'' ~[\']* '\''
	| '"' ~["]* '"';

NUMERIC_LITERAL
    : INTEGER ('.' DIGITS)? EXPONENT_PART?;
	
ENUM_LITERAL
	: ENUM_NAME '.' ENUM_KEY;
	
ENUM_COLLECTION_LITERAL
	: ENUM_NAME '.' 'of' '(' ENUM_KEY (',' ENUM_KEY)* ')';

ENUM_NAME
	: ENUM_GENDER
	| ENUM_DEVICE_TYPE;
	
fragment ENUM_KEY
	: [A-Z_]+;
    
fragment INTEGER
	: '0' 
	| DIGIT_NOT_ZERO DIGITS?;
    
fragment EXPONENT_PART
   :   [eE] SIGNED_INTEGER
   ;

fragment SIGNED_INTEGER
   :   [+-]? DIGITS
   ;

// Functions

SIN : S I N;
COS : C O S;
TAN : T A N;
EXP : E X P;
LOG : L O G;
POW : P O W;
SQRT : S Q R T;

OP_LT : '<';
OP_LE : '<=';
OP_GT : '>';
OP_GE : '>=';
OP_EQ : '=';
OP_NEQ1 : '!=';
OP_NEQ2 : '<>';
OP_PLUS : '+';
OP_MINUS : '-';
OP_MUL : '*';
OP_DIV : '/';

LP : '(';
RP : ')';
COMMA: ',';
DOT: '.';

Identifier
    : JavaLetter JavaLetterOrDigit*
    ;

// Enums

ENUM_GENDER : 'Gender';
ENUM_DEVICE_TYPE : 'Device';

WS: [ \n\t\r]+ -> channel(HIDDEN);

fragment A: 'A';//('a'|'A');
fragment B: 'B';//('b'|'B');
fragment C: 'C';//('c'|'C');
fragment D: 'D';//('d'|'D');
fragment E: 'E';//('e'|'E');
fragment F: 'F';//('f'|'F');
fragment G: 'G';//('g'|'G');
fragment H: 'H';//('h'|'H');
fragment I: 'I';//('i'|'I');
fragment J: 'J';//('j'|'J');
fragment K: 'K';//('k'|'K');
fragment L: 'L';//('l'|'L');
fragment M: 'M';//('m'|'M');
fragment N: 'N';//('n'|'N');
fragment O: 'O';//('o'|'O');
fragment P: 'P';//('p'|'P');
fragment Q: 'Q';//('q'|'Q');
fragment R: 'R';//('r'|'R');
fragment S: 'S';//('s'|'S');
fragment T: 'T';//('t'|'T');
fragment U: 'U';//('u'|'U');
fragment V: 'V';//('v'|'V');
fragment W: 'W';//('w'|'W');
fragment X: 'X';//('x'|'X');
fragment Y: 'Y';//('y'|'Y');
fragment Z: 'Z';//('z'|'Z');

fragment
JavaLetter
: [a-zA-Z$_] // these are the "java letters" below 0xFF
| // covers all characters above 0xFF which are not a surrogate
~[\u0000-\u00FF\uD800-\uDBFF]
{Character.isJavaIdentifierStart(_input.LA(-1))}?
| // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
[\uD800-\uDBFF] [\uDC00-\uDFFF]
{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
;

fragment
JavaLetterOrDigit
: [a-zA-Z0-9$_] // these are the "java letters or digits" below 0xFF
| // covers all characters above 0xFF which are not a surrogate
~[\u0000-\u00FF\uD800-\uDBFF]
{Character.isJavaIdentifierPart(_input.LA(-1))}?
| // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
[\uD800-\uDBFF] [\uDC00-\uDFFF]
{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
;