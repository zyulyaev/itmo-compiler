package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public interface AsgExpressionVisitor<T> {
    T visit(AsgLiteralExpression<?> literal);

    T visit(AsgBinaryExpression binaryExpression);

    T visit(AsgFunctionCallExpression functionCall);

    T visit(AsgMethodCallExpression methodCall);

    T visit(AsgArrayExpression arrayExpression);

    T visit(AsgIndexExpression indexExpression);

    T visit(AsgMemberAccessExpression memberAccessExpression);

    T visit(AsgVariableExpression variableExpression);

    T visit(AsgCastExpression castExpression);

    T visit(AsgDataExpression dataExpression);
}
