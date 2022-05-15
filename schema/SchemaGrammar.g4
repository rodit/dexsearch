grammar SchemaGrammar;

WS: [ \t\n\r]+ -> skip;

HASH: '#' ;
DIRECTIVE_OPTION: 'option' ;

fragment SINGLE_QUOTE : '\'' ;
fragment DOUBLE_QUOTE : '"' ;
fragment ESCAPED_CHAR: '\\0'
    | '\\' 'b'
    | '\\' 'n'
    | '\\' 'f'
    | '\\' 'r'
    | '\\' DOUBLE_QUOTE
    | '\\' SINGLE_QUOTE
    | '\\' '\\' ;
fragment DIGIT: '0'..'9' ;
fragment CHARACTER: ~('\''
    | '"'
    | '\\')
    | ESCAPED_CHAR ;

INT_LIT: DIGIT+ ;
BOOL_LIT: 'true'
    | 'false' ;
CHAR_LIT: '\'' CHARACTER '\'' ;
STRING_LIT: '"' CHARACTER* '"' ;
FLOAT_LIT: DIGIT+ '.' DIGIT* ;

IMPORT: 'import' ;
EXPECTS: 'expects' ;
THIS: 'this' ;

CLASS_TYPE: 'class'
    | 'enum'
    | 'interface' ;

ACCESS_FLAG: 'public'
    | 'private'
    | 'protected'
    | 'static'
    | 'final'
    | 'synchronized'
    | 'volatile'
    | 'bridge'
    | 'transient'
    | 'native'
    | 'abstract'
    | 'synthetic'
    | 'constructor' ;

PRIMITIVE: 'boolean'
    | 'byte'
    | 'char'
    | 'short'
    | 'int'
    | 'long'
    | 'float'
    | 'double'
    | 'void' ;

BIND_EVENT_OPERATION: 'bind' ;
BIND_EVENT_TARGET: 'field'
    | 'method'
    | 'type' ;
BIND_EVENT_MODIFIER: 'previous'
    | 'current'
    | 'next' ;
BIND_EVENT_SOURCE: 'reference' ;

BYTECODE_STRING: '.string' ;
BYTECODE_STRING_REGEX: 'regex' ;
BYTECODE_STRING_CONTAINS: 'contains' ;
BYTECODE_TYPE_REFERENCE: '.type' ;
BYTECODE_METHOD_REFERENCE: '.method' ;
BYTECODE_FIELD_REFERENCE: '.field' ;
BYTECODE_MEMBER_SEPARATOR: '->' ;
BYTECODE_EXPRESSION: '.expr' ;
BYTECODE_REGISTER_PREFIX: '.r' ;
BYTECODE_PARAM_PREFIX: '.p' ;
BYTECODE_NEW_INSTANCE: '.new' ;
BYTECODE_CONSTANT: '.const' ;

COMMA: ',' ;
OPEN_SQUARE: '[' ;
CLOSE_SQUARE: ']' ;
OPEN_BRACE: '{' ;
CLOSE_BRACE: '}' ;
OPEN_BRACKET: '(' ;
CLOSE_BRACKET: ')' ;
OPEN_TRI: '<' ;
CLOSE_TRI: '>' ;
SEMICOLON: ';' ;
EXCLAMATION: '!' ;
STAR: '*' ;
VARARGS_ANY: '...' ;
DOLLAR: '$' ;
EQUALS: '=' ;

ANNOTATION_PREFIX: '@' ;
EXTENDS: 'extends' ;
IMPLEMENTS: 'implements' ;

IDENTIFIER: [_a-zA-Z0-9<>]+ ;
JAVA_TYPE_IDENTIFIER: IDENTIFIER ('.' IDENTIFIER)* (DOLLAR IDENTIFIER)? ;

javaTypeName: IDENTIFIER | JAVA_TYPE_IDENTIFIER ;

constant: STRING_LIT #stringLiteral
    | BOOL_LIT #boolLiteral
    | INT_LIT #intLiteral
    | CHAR_LIT #charLiteral
    | FLOAT_LIT #floatLiteral ;

identifierList: IDENTIFIER
    | IDENTIFIER (COMMA IDENTIFIER)* ;
schemaAttributes: OPEN_SQUARE identifierList CLOSE_SQUARE ;

directive: HASH DIRECTIVE_OPTION IDENTIFIER IDENTIFIER #optionDirective ;

annotationArguments: constant?
    | constant (COMMA constant)* ;
annotation: ANNOTATION_PREFIX type OPEN_BRACKET annotationArguments CLOSE_BRACKET ;

definitionPrefix: schemaAttributes? annotation* ACCESS_FLAG* ;

name: DOLLAR? IDENTIFIER ;

fieldDefinition: definitionPrefix type name SEMICOLON ;

type: OPEN_TRI EXTENDS type CLOSE_TRI #extendsType
    | OPEN_TRI IMPLEMENTS typeList CLOSE_TRI #implementsType
    | THIS #thisType
    | EXCLAMATION IDENTIFIER #referencedType
    | HASH IDENTIFIER #outType
    | javaTypeName #javaType
    | PRIMITIVE #primitiveType
    | STAR #anyType
    | type OPEN_SQUARE CLOSE_SQUARE #arrayType
    | VARARGS_ANY #varargsType ;

typeList: type?
    | type (COMMA type)* ;

bindEvent: BIND_EVENT_OPERATION BIND_EVENT_TARGET IDENTIFIER BIND_EVENT_MODIFIER BIND_EVENT_SOURCE ;

bytecodeExpression: bytecodeMemberReference EQUALS STRING_LIT #setStringExpression
    | bytecodeMemberReference EQUALS INT_LIT #setIntExpression
    | bytecodeMemberReference EQUALS BYTECODE_REGISTER_PREFIX INT_LIT #setRegisterExpression
    | bytecodeMemberReference EQUALS BYTECODE_PARAM_PREFIX INT_LIT #setParamExpression ;
bytecodeMemberName: IDENTIFIER #normalMemberName
    | EXCLAMATION IDENTIFIER #referenceMemberName
    | STAR #anyMemberName ;
bytecodeMemberReference: type BYTECODE_MEMBER_SEPARATOR bytecodeMemberName ;
methodBodyString: BYTECODE_STRING STRING_LIT #bodyStringAny
    | BYTECODE_STRING BYTECODE_STRING_CONTAINS STRING_LIT #bodyStringContains
    | BYTECODE_STRING BYTECODE_STRING_REGEX STRING_LIT #bodyStringRegex ;
methodBodyTypeReference: BYTECODE_TYPE_REFERENCE type ;
methodBodyMethodReference: BYTECODE_METHOD_REFERENCE bytecodeMemberReference ;
methodBodyFieldReference: BYTECODE_FIELD_REFERENCE bytecodeMemberReference ;
methodBodyNewInstance: BYTECODE_NEW_INSTANCE type ;
methodBodyExpression: BYTECODE_EXPRESSION bytecodeExpression ;
methodBodyConstant: BYTECODE_CONSTANT INT_LIT ;
methodBodyMatcher: definitionPrefix (methodBodyString
    | methodBodyTypeReference
    | methodBodyMethodReference
    | methodBodyFieldReference
    | methodBodyNewInstance
    | methodBodyExpression
    | methodBodyConstant
    ) (COMMA bindEvent)* SEMICOLON ;

methodBody: OPEN_BRACE methodBodyMatcher* CLOSE_BRACE ;
methodDefinition: definitionPrefix type name OPEN_BRACKET typeList CLOSE_BRACKET methodBody? ;

expectsStatement: EXPECTS javaTypeName ;
extendsStatement: EXTENDS type ;
implementsStatement: IMPLEMENTS type (COMMA type)* ;

classDefinition: definitionPrefix CLASS_TYPE name expectsStatement? extendsStatement? implementsStatement? OPEN_BRACE fieldDefinition* methodDefinition* CLOSE_BRACE ;

importStatement: IMPORT JAVA_TYPE_IDENTIFIER SEMICOLON ;

schema: directive* importStatement* classDefinition* ;