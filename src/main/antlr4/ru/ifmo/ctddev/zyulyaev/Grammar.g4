grammar Grammar;

program
    : statement *
    ;

statement
    : functionCall
    | assignment
    ;

functionCall: id '(' arguments ')';

arguments
    : argument
    | argument ',' arguments
    ;

argument: expression;

assignment: id ':=' expression;

expression
    : left=expression op=('*'|'/') right=expression     # multExpr
    | left=expression op=('+'|'-') right=expression     # sumExpr
    | functionCall                                      # functionExpr
    | id                                                # idExpr
    | literal                                           # literalExpr
    ;

id: ID;
literal: STR | INT;

STR : '"' (~'"'|'\\"')* '"';
ID: [a-zA-Z_]+;
INT: [0-9]+;
WS: [\n\r\t ]+ -> skip;
