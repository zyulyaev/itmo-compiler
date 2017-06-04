package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public interface AsgExpression {
    AsgType getResultType();

    <T> T accept(AsgExpressionVisitor<T> visitor);
}
