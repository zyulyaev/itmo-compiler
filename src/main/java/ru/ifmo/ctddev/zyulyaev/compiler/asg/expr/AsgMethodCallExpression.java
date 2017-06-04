package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.List;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Data
public class AsgMethodCallExpression implements AsgExpression {
    private final AsgExpression object;
    private final AsgMethod method;
    private final List<AsgExpression> arguments;

    // todo check that object belongs to method's class

    @Override
    public AsgType getResultType() {
        return method.getReturnType();
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
