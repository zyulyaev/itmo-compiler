package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
public interface AsgLeftValueExpressionVisitor<T> extends AsgExpressionVisitor<T> {
    @Override
    default T visit(AsgLiteralExpression<?> literal) {
        throw new IllegalStateException();
    }

    @Override
    default T visit(AsgBinaryExpression binaryExpression) {
        throw new IllegalStateException();
    }

    @Override
    default T visit(AsgFunctionCallExpression functionCall) {
        throw new IllegalStateException();
    }

    @Override
    default T visit(AsgMethodCallExpression methodCall) {
        throw new IllegalStateException();
    }

    @Override
    default T visit(AsgArrayExpression arrayExpression) {
        throw new IllegalStateException();
    }
}
