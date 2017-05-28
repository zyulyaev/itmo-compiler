package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;

import java.util.List;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class AsgArrayExpression implements AsgExpression {
    private final List<AsgExpression> values;

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
