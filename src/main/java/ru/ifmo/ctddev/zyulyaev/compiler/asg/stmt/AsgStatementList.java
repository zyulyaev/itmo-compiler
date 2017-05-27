package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

import lombok.Data;

import java.util.List;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgStatementList implements AsgStatement {
    private final List<AsgStatement> statements;

    @Override
    public <T> T accept(AsgStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
