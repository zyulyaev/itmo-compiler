package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public interface AsgExpression {
    <T> T accept(AsgExpressionVisitor<T> visitor);
}
