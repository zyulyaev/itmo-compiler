grammar Grammar;

program: definitions statements;

definitions: functionDefinition*;

functionDefinition: 'fun' name=id '(' parameters ')' 'begin' body=statements 'end';

parameters
    : parameter (',' parameter)*
    | // empty parameters
    ;

parameter: id;

statements: statement (';' statement)*;

statement
    : functionCall
    | assignment
    | ifStatement
    | forStatement
    | whileStatement
    | repeatStatement
    | returnStatement
    | skipStatement
    | // empty statement
    ;

functionCall: name=id '(' args=arguments ')';

arguments
    : argument (',' argument)*
    | // empty arguments
    ;

argument: expression;

assignment: variable=leftValue ':=' value=expression;

ifStatement: 'if' condition=expression 'then' positive=statements negative=elseBlock? 'fi';
elseBlock
    : 'else' negative=statements                                                    # plainElse
    | 'elif' condition=expression 'then' positive=statements negative=elseBlock?    # elif
    ;

forStatement: 'for' init=forInitialization ',' term=expression ',' inc=forIncrement 'do' body=statements 'od';
forInitialization: assignment | skipStatement;
forIncrement: assignment | skipStatement;

whileStatement: 'while' condition=expression 'do' body=statements 'od';

repeatStatement: 'repeat' body=statements 'until' condition=expression;

returnStatement: 'return' value=expression;

skipStatement: 'skip';

expression
    : left=expression op=('*'|'/'|'%') right=expression                     # binExpr
    | left=expression op=('+'|'-') right=expression                         # binExpr
    | left=expression op=('<'|'>'|'>='|'<='|'=='|'!=') right=expression     # binExpr
    | left=expression op=('&&'|'||') right=expression                       # binExpr
    | left=expression op='!!' right=expression                              # binExpr
    | '(' expression ')'                                                    # parensExpr
    | functionCall                                                          # functionExpr
    | leftValue                                                             # leftValueExpr
    | literal                                                               # literalExpr
    | '[' arguments ']'                                                     # arrayExpr
    ;

leftValue: value=id ('[' expression ']')*;

id: ID;
literal
    : STR       # stringLiteral
    | INT       # intLiteral
    | CHAR      # charLiteral
    | BOOL      # boolLiteral
    | NULL      # nullLiteral
    ;

STR : '"' (~'"'|'\\"')* '"';
BOOL: 'true' | 'false';
ID: [a-zA-Z_][a-zA-Z0-9_]*;
INT: [0-9]+;
CHAR: '\'' (~'\''|'\\\'') '\'';
NULL: '{}';
WS: [\n\r\t ]+ -> skip;
