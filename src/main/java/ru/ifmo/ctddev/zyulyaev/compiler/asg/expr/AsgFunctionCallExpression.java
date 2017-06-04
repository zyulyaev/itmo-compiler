package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

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
        if (function.getParameterTypes().size() != arguments.size()) {
            throw new IllegalArgumentException("Arguments and parameters count does not match");
        }
        for (int i = 0; i < arguments.size(); i++) {
            if (!function.getParameterTypes().get(i).isAssignableFrom(arguments.get(i).getResultType())) {
                throw new IllegalArgumentException("Function parameter types don't match");
            }
        }
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public AsgType getResultType() {
        return function.getReturnType();
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
