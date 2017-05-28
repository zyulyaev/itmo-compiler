package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;

import java.util.List;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgLeftValueExpression implements AsgExpression {
    private final AsgVariable variable;
    private final List<AsgExpression> indexes;

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
