package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
@Data
public class AsgVariableAssignment implements AsgStatement {
    private final AsgVariable variable;
    private final AsgExpression value;

    public AsgVariableAssignment(AsgVariable variable, AsgExpression value) {
        if (!variable.getType().isAssignableFrom(value.getResultType())) {
            throw new IllegalArgumentException(variable.getType() + " is not assignable from " + value.getResultType());
        }
        this.variable = variable;
        this.value = value;
    }

    @Override
    public <T> T accept(AsgStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
