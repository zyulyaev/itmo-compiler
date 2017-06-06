grammar Grammar;

program: definitions statements;

definitions: topLevelDefinition*;

topLevelDefinition
    : functionDefinition
    | dataDefinition
    | classDefinition
    | implDefinition
    ;

functionDefinition: 'fun' name=id '(' parameters ')' ':' returnType=type 'begin' body=statements 'end';

parameters
    : parameter (',' parameter)*
    | // empty parameters
    ;

parameter: id ':' type;

dataDefinition: 'data' name=id '{' fields '}';

fields
    : field (',' field)*
    | // no fields
    ;
field: name=id ':' type;

classDefinition: 'class' name=id '{' methodDecl* '}';

methodDecl: 'fun' name=id '(' parameters ')' ':' returnType=type;

implDefinition: 'impl' className=id 'for' dataType=type '{' functionDefinition* '}';

type
    : id            # plainType
    | '[' type ']'  # arrayType
    ;

statements: statement (';' statement)*;

statement
    : expressionStatement
    | assignment
    | ifStatement
    | forStatement
    | whileStatement
    | repeatStatement
    | returnStatement
    | skipStatement
    | // empty statement
    ;

expressionStatement: expression;

assignment
    : variable=id ':=' value=expression     # variableAssignment
    | leftValue ':=' value=expression       # leftValueAssignment
    ;

ifStatement: 'if' condition=expression 'then' positive=statements negative=elseBlock? 'fi';
elseBlock
    : 'else' negative=statements                                                    # plainElse
    | 'elif' condition=expression 'then' positive=statements negative=elseBlock?    # elif
    ;

forStatement: 'for' initializaion=forInitialization ',' termination=expression ',' increment=forIncrement 'do' body=statements 'od';
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
    | left=expression op='&&' right=expression                              # binExpr
    | left=expression op='!!' right=expression                              # binExpr
    | '[' arguments ']'                                                     # arrayExpr
    | dataExpression                                                        # dataExpr
    | expression 'as' type                                                  # castExpr
    | leftValue                                                             # leftValueExpr
    | term                                                                  # termExpr
    ;

leftValue
    :<assoc=right> array=leftValue '[' index=expression ']'                 # indexLeftValue1
    |<assoc=right> array=term      '[' index=expression ']'                 # indexLeftValue2
    |<assoc=right> object=leftValue '.' member=id                           # memberAccessLeftValue1
    |<assoc=right> object=term '.' member=id                                # memberAccessLeftValue2
    ;

term
    : '(' expression ')'                                                    # parensTerm
    | name=id '(' args=arguments ')'                                        # functionCallTerm
    | <assoc=right> object=term '.' method=id '(' args=arguments ')'        # methodCallTerm
    | literal                                                               # literalTerm
    | id                                                                    # idTerm
    ;

dataExpression: dataType=id '{' fieldExpression (',' fieldExpression)* '}';

fieldExpression: name=id ':' value=expression;

arguments
    : argument (',' argument)*
    | // empty arguments
    ;

argument: expression;

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
