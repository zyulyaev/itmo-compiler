package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgLiteralExpression<T> implements AsgExpression {
    private final LiteralType type;
    private final T value;

    @Override
    public <U> U accept(AsgExpressionVisitor<U> visitor) {
        return visitor.visit(this);
    }

    public enum LiteralType {
        INT, STRING, NULL
    }
}
