package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;

import java.util.List;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgFunctionCallExpression implements AsgExpression {
    private final AsgFunction function;
    private final List<AsgExpression> arguments;

    public AsgFunctionCallExpression(AsgFunction function, List<AsgExpression> arguments) {
        if (function.getParameters().size() != arguments.size()) {
            throw new IllegalArgumentException("Arguments and parameters count does not match");
        }
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
