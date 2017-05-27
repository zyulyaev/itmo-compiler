package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;

import javax.annotation.Nullable;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgIfStatement implements AsgStatement {
    private final AsgExpression condition;
    private final AsgStatement positive;
    @Nullable
    private final AsgStatement negative;

    @Override
    public <T> T accept(AsgStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
