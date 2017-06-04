package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public interface AsgStatementVisitor<T> {
    T visit(AsgAssignment assignment);

    T visit(AsgVariableAssignment assignment);

    T visit(AsgIfStatement ifStatement);

    T visit(AsgStatementList statementList);

    T visit(AsgForStatement forStatement);

    T visit(AsgWhileStatement whileStatement);

    T visit(AsgRepeatStatement repeatStatement);

    T visit(AsgExpressionStatement expressionStatement);

    T visit(AsgReturnStatement returnStatement);
}
