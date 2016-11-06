grammar Bonobo;

WS: (' ' | '\n' | '\r' | '\r\n') -> skip;
SL_CMT: '//' ~('\n')* -> channel(HIDDEN);

BRACKETS: '[]';

INT: '-'? [0-9]+;
STRING: '\'' ('\\\'' | ~('\n' | '\''))* '\'';
ID: [a-z] [A-Za-z0-9_]*;

compilationUnit: ('import' importSource)* topLevelDef*;

importSource:
    STRING #StringSource
    | '<' name=ID '>' #GlobalSource
;

topLevelDef:
    'fn' funcSignature funcBody #TopLevelFuncDef
    | 'const' (variableDeclaration ',')* variableDeclaration ';'* #ConstDef
;

funcSignature: name=ID '(' ((params+=paramSpec ',')* params+=paramSpec)? ')' (':' returnType=type)?;

type:
    'int' #IntType
    | type BRACKETS #TupleType
;

funcBody:
    block #BlockBody
    | '=>' stmt ';'* #StmtBody
    | '=>' expr ';'* #ExprBody
;

paramSpec:
    ID #SimpleParamSpec
    | 'fn' funcSignature #FunctionParamSpec
    | ID BRACKETS+ #TupleParamSpec
;

expr:
    ID #IdentifierExpr
    | INT #IntegerLiteralExpr
    | STRING #StringExpr
    | '(' lower=expr '.' '.' exclusive='.'? upper=expr ')' #RangeLiteralExpr
    | '(' (expr ',')+ expr ')' #TupleLiteralExpr
    | callee=ID '(' ((args+=expr ',')* args+=expr)? ')' #InvocationExpr
    | left=expr right=expr #AdjacentExprs
    | left=expr assignmentOp right=expr #AssignmentExpr
    | left=expr '^' right=expr #PowerExpr
    | left=expr op=('*'|'/') right=expr #MultiplicativeExpr
    | left=expr op=('+'|'-') right=expr #AdditiveExpr
    | left=expr '%' right=expr #ModuloExpr
    | left=expr ('±'|'+-') right=expr #PlusMinusExpr
    | left=expr '==' right=expr #EqualsExpr
    | left=expr '!=' right=expr #NotEqualsExpr
    | left=expr '&&' right=expr #AndExpr
    | left=expr '||' right=expr #OrExpr
    | '(' expr ')' #ParenthesizedExpr
;

assignmentOp: '=' | '^=' | '*=' | '/=' | '+=' | '-=' | '%=';

block: (stmt ';'*) | ('{' (stmt ';'*)* '}');

stmt:
    left=expr assignmentOp right=expr #AssignmentStmt
    | 'for' '(' name=ID ':' in=expr ')' block #ForEachStmt
    | 'for' '(' 'let' name=ID '=' init=expr ';' cond=expr ';' incrementer=stmt ')' block #ForStmt
    | helper=('print'|'printf'|'debug'|'debugf') (args+=expr ',')* args+=expr #HelperFuncStmt
    | ifBlock ('else' ifBlock)* elseBlock? #IfStmt
    | 'do' block 'while' '(' cond=expr ')' #DoWhileStmt
    | 'while' '(' cond=expr ')' block #WhileStmt
    | specifier=('let'|'mut') (variableDeclaration ',')* variableDeclaration #VarDeclStmt
    | 'let' funcSignature '=' expr #InlineFuncDeclStmt
    | 'return' expr #ReturnStmt
;

ifBlock: 'if' '(' cond=expr ')' block;
elseBlock: 'else' block;

variableDeclaration: name=ID '=' expr;