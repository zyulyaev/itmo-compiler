package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLeftValueExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgAssignment;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgForStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgFunctionCallStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgIfStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgRepeatStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgReturnStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementList;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgWhileStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcVariableCollector implements AsgStatementVisitor<Void> {
    private final Map<AsgVariable, BcVariable> variables = new HashMap<>();
    private final Set<AsgVariable> ignore;

    public BcVariableCollector(Set<AsgVariable> ignore) {
        this.ignore = ignore;
    }

    public Map<AsgVariable, BcVariable> getVariables() {
        return variables;
    }

    @Override
    public Void visit(AsgAssignment assignment) {
        AsgLeftValueExpression leftValue = assignment.getLeftValue();
        if (leftValue.getIndexes().isEmpty()) {
            AsgVariable variable = leftValue.getVariable();
            if (!ignore.contains(variable)) {
                variables.computeIfAbsent(variable, var -> new BcVariable(var.getName()));
            }
        }
        return null;
    }

    @Override
    public Void visit(AsgIfStatement ifStatement) {
        ifStatement.getPositive().accept(this);
        if (ifStatement.getNegative() != null) {
            ifStatement.getNegative().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(AsgStatementList statementList) {
        for (AsgStatement statement : statementList.getStatements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(AsgForStatement forStatement) {
        forStatement.getInitialization().accept(this);
        forStatement.getIncrement().accept(this);
        forStatement.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(AsgWhileStatement whileStatement) {
        whileStatement.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(AsgRepeatStatement repeatStatement) {
        repeatStatement.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(AsgFunctionCallStatement functionCallStatement) {
        return null;
    }

    @Override
    public Void visit(AsgReturnStatement returnStatement) {
        return null;
    }
}
