package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgVariableExpression implements AsgExpression {
    private final AsgVariable variable;

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
