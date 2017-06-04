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

    public AsgMethodCallExpression(AsgExpression object, AsgMethod method, List<AsgExpression> arguments) {
        if (!method.getParent().isAssignableFrom(object.getResultType())) {
            throw new IllegalArgumentException(object.getResultType() + " does not implement " + method.getParent());
        }
        if (method.getParameterTypes().size() != arguments.size()) {
            throw new IllegalArgumentException("Arguments and parameters count does not match");
        }
        for (int i = 0; i < arguments.size(); i++) {
            if (!method.getParameterTypes().get(i).isAssignableFrom(arguments.get(i).getResultType())) {
                throw new IllegalArgumentException("Method parameter types don't match");
            }
        }
        this.object = object;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public AsgType getResultType() {
        return method.getReturnType();
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
