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

    @Override
    public <T> T accept(AsgStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
