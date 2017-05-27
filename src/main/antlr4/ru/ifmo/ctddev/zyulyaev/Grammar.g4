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

assignment: variable=id ':=' value=expression;

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
    | '(' expression ')'                                                    # parensExpr
    | functionCall                                                          # functionExpr
    | id                                                                    # idExpr
    | literal                                                               # literalExpr
    ;

id: ID;
literal
    : STR       # stringLiteral
    | INT       # intLiteral
    ;

STR : '"' (~'"'|'\\"')* '"';
ID: [a-zA-Z_][a-zA-Z0-9_]*;
INT: [0-9]+;
WS: [\n\r\t ]+ -> skip;
