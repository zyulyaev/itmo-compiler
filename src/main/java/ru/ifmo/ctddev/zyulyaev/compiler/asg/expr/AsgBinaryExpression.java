package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgBinaryOperator;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgBinaryExpression implements AsgExpression {
    private final AsgExpression left;
    private final AsgExpression right;
    private final AsgBinaryOperator operator;

    public AsgBinaryExpression(AsgExpression left, AsgExpression right, AsgBinaryOperator operator) {
        if (left.getResultType() != AsgPredefinedType.INT || right.getResultType() != AsgPredefinedType.INT) {
            throw new IllegalArgumentException("Binary operator is only applicable on ints");
        }
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public AsgType getResultType() {
        return AsgPredefinedType.INT;
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
