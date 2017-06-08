package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgReturnStatement implements AsgStatement {
    private final AsgType returnType;
    private final AsgExpression value;

    public AsgReturnStatement(AsgType returnType, AsgExpression value) {
        if (!value.getResultType().equals(returnType)) {
            throw new IllegalArgumentException("Types don't match");
        }
        this.returnType = returnType;
        this.value = value;
    }

    @Override
    public <T> T accept(AsgStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
