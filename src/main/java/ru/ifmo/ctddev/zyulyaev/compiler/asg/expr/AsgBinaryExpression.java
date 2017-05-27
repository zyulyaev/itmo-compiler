package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.BinaryOperator;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgBinaryExpression implements AsgExpression {
    private final AsgExpression left;
    private final AsgExpression right;
    private final BinaryOperator operator;

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
