package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Data
public class AsgExpressionStatement implements AsgStatement {
    private final AsgExpression expression;

    @Override
    public <T> T accept(AsgStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
