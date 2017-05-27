package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgReturnStatement implements AsgStatement {
    private final AsgExpression value;

    @Override
    public <T> T accept(AsgStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
