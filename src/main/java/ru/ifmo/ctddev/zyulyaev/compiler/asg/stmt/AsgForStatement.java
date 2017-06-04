package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgForStatement implements AsgStatement {
    private final AsgStatement initialization;
    private final AsgExpression termination;
    private final AsgStatement increment;
    private final AsgStatement body;

    public AsgForStatement(AsgStatement initialization, AsgExpression termination, AsgStatement increment,
        AsgStatement body)
    {
        if (termination.getResultType() != AsgPredefinedType.INT) {
            throw new IllegalArgumentException("Termination condition must be int type");
        }
        this.initialization = initialization;
        this.termination = termination;
        this.increment = increment;
        this.body = body;
    }

    @Override
    public <T> T accept(AsgStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
