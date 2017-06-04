package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
@Data
public class AsgVariableExpression implements AsgLeftValueExpression {
    private final AsgVariable variable;

    @Override
    public AsgType getResultType() {
        return variable.getType();
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
