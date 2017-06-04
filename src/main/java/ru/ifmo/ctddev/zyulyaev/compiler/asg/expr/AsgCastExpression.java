package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
@Data
public class AsgCastExpression implements AsgExpression {
    private final AsgExpression expression;
    private final AsgType target;
    private final boolean explicit;

    public AsgCastExpression(AsgExpression expression, AsgType target, boolean explicit) {
        if (!target.isAssignableFrom(expression.getResultType())) {
            throw new IllegalArgumentException("Cannot cast " + expression.getResultType() + " to " + target);
        }
        this.expression = expression;
        this.target = target;
        this.explicit = explicit;
    }

    @Override
    public AsgType getResultType() {
        return target;
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
