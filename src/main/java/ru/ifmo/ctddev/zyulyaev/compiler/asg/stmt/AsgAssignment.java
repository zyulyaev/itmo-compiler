package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLeftValueExpression;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgAssignment implements AsgStatement {
    private final AsgLeftValueExpression leftValue;
    private final AsgExpression expression;

    public AsgAssignment(AsgLeftValueExpression leftValue, AsgExpression expression) {
        if (!leftValue.getResultType().equals(expression.getResultType())) {
            throw new IllegalArgumentException("Cannot assign " + leftValue.getResultType() +
                " from " + expression.getResultType());
        }
        this.leftValue = leftValue;
        this.expression = expression;
    }

    @Override
    public <T> T accept(AsgStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
