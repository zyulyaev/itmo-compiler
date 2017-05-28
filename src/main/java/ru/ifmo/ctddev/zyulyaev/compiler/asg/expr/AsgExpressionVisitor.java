package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public interface AsgExpressionVisitor<T> {
    T visit(AsgLiteralExpression<?> literal);

    T visit(AsgLeftValueExpression leftValue);

    T visit(AsgBinaryExpression binaryExpression);

    T visit(AsgFunctionCallExpression functionCall);

    T visit(AsgArrayExpression arrayExpression);
}
