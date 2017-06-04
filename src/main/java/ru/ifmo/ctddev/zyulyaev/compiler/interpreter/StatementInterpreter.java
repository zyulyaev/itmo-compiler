package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgAssignment;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgExpressionStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgForStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgIfStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgRepeatStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgReturnStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementList;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgVariableAssignment;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgWhileStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.LeftValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class StatementInterpreter implements AsgStatementVisitor<Value> {
    private final InterpreterContext context;

    StatementInterpreter(InterpreterContext context) {
        this.context = context;
    }

    @Override
    public Value visit(AsgAssignment assignment) {
        LeftValue leftValue = assignment.getLeftValue().accept(context.asExpressionInterpreter()).asLeftValue();
        leftValue.set(assignment.getExpression().accept(context.asExpressionInterpreter()).asRightValue());
        return null;
    }

    @Override
    public Value visit(AsgVariableAssignment assignment) {
        context.getOrDefineValue(assignment.getVariable())
            .set(assignment.getValue().accept(context.asExpressionInterpreter()).asRightValue());
        return null;
    }

    @Override
    public Value visit(AsgIfStatement ifStatement) {
        StatementInterpreter ifInterpreter = createChild();
        if (ifInterpreter.interpretCondition(ifStatement.getCondition())) {
            return ifStatement.getPositive().accept(ifInterpreter);
        } else if (ifStatement.getNegative() != null) {
            return ifStatement.getNegative().accept(ifInterpreter);
        }
        return null;
    }

    @Override
    public Value visit(AsgStatementList statementList) {
        for (AsgStatement statement : statementList.getStatements()) {
            Value result = statement.accept(this);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Value visit(AsgForStatement forStatement) {
        StatementInterpreter forInterpreter = createChild();
        forStatement.getInitialization().accept(forInterpreter);
        while (forInterpreter.interpretCondition(forStatement.getTermination())) {
            Value result = forStatement.getBody().accept(forInterpreter);
            if (result != null) {
                return result;
            }
            forStatement.getIncrement().accept(forInterpreter);
        }
        return null;
    }

    @Override
    public Value visit(AsgWhileStatement whileStatement) {
        StatementInterpreter whileInterpreter = createChild();
        while (whileInterpreter.interpretCondition(whileStatement.getCondition())) {
            Value result = whileStatement.getBody().accept(whileInterpreter);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Value visit(AsgRepeatStatement repeatStatement) {
        StatementInterpreter repeatInterpreter = createChild();
        do {
            Value result = repeatStatement.getBody().accept(repeatInterpreter);
            if (result != null) {
                return result;
            }
        } while (!repeatInterpreter.interpretCondition(repeatStatement.getCondition()));
        return null;
    }

    @Override
    public Value visit(AsgExpressionStatement expressionStatement) {
        expressionStatement.getExpression().accept(context.asExpressionInterpreter());
        return null;
    }

    @Override
    public Value visit(AsgReturnStatement returnStatement) {
        return returnStatement.getValue().accept(context.asExpressionInterpreter());
    }

    private StatementInterpreter createChild() {
        return context.createChild().asStatementInterpreter();
    }

    private boolean interpretCondition(AsgExpression expression) {
        IntValue value = expression.accept(context.asExpressionInterpreter()).asRightValue().asInt();
        return value.getValue() != 0;
    }
}
