package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgFunctionCallExpression;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgFunctionCallStatement implements AsgStatement {
    private final AsgFunctionCallExpression expression;

    @Override
    public <T> T accept(AsgStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
