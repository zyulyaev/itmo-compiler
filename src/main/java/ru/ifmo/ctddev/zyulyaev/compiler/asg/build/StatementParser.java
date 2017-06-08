package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.GrammarBaseVisitor;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgCastExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLeftValueExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgVariableExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgAssignment;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgExpressionStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgForStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgIfStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgRepeatStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgReturnStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementList;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgWhileStatement;

import java.util.ArrayList;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class StatementParser extends GrammarBaseVisitor<AsgStatement> {
    private final Context context;

    StatementParser(Context context) {
        this.context = context;
    }

    @Override
    public AsgStatement visitVariableAssignment(GrammarParser.VariableAssignmentContext ctx) {
        String name = ctx.variable.getText();
        AsgExpression expression = ctx.value.accept(context.asExpressionParser());
        AsgVariable variable = context.resolveOrDeclareVariable(name, expression.getResultType(), false);
        return new AsgAssignment(new AsgVariableExpression(variable), expression);
    }

    @Override
    public AsgStatement visitLeftValueAssignment(GrammarParser.LeftValueAssignmentContext ctx) {
        AsgLeftValueExpression leftValue =
            (AsgLeftValueExpression) ctx.leftValue().accept(context.asExpressionParser());
        AsgExpression expression = ctx.value.accept(context.asExpressionParser());
        return new AsgAssignment(leftValue, expression);
    }

    @Override
    public AsgStatement visitExpressionStatement(GrammarParser.ExpressionStatementContext ctx) {
        return new AsgExpressionStatement(ctx.expression().accept(context.asExpressionParser()));
    }

    @Override
    public AsgStatement visitIfStatement(GrammarParser.IfStatementContext ctx) {
        AsgExpression condition = ctx.condition.accept(context.asExpressionParser());
        AsgStatement positive = ctx.positive.accept(context.asStatementParser());
        AsgStatement negative = null;
        if (ctx.negative != null) {
            negative = ctx.negative.accept(context.asStatementParser());
        }
        return new AsgIfStatement(condition, positive, negative);
    }

    @Override
    public AsgStatement visitElif(GrammarParser.ElifContext ctx) {
        AsgExpression condition = ctx.condition.accept(context.asExpressionParser());
        AsgStatement positive = ctx.positive.accept(this);
        AsgStatement negative = null;
        if (ctx.negative != null) {
            negative = ctx.negative.accept(context.asStatementParser());
        }
        return new AsgIfStatement(condition, positive, negative);
    }

    @Override
    public AsgStatement visitForStatement(GrammarParser.ForStatementContext ctx) {
        AsgStatement initialization = ctx.initializaion.accept(this);
        AsgExpression termination = ctx.termination.accept(context.asExpressionParser());
        AsgStatement increment = ctx.increment.accept(this);
        AsgStatement body = ctx.body.accept(this);
        return new AsgForStatement(initialization, termination, increment, body);
    }

    @Override
    public AsgStatement visitWhileStatement(GrammarParser.WhileStatementContext ctx) {
        AsgExpression condition = ctx.condition.accept(context.asExpressionParser());
        AsgStatement body = ctx.body.accept(this);
        return new AsgWhileStatement(condition, body);
    }

    @Override
    public AsgStatement visitRepeatStatement(GrammarParser.RepeatStatementContext ctx) {
        AsgStatement body = ctx.body.accept(this);
        AsgExpression condition = ctx.condition.accept(context.asExpressionParser());
        return new AsgRepeatStatement(condition, body);
    }

    @Override
    public AsgStatement visitReturnStatement(GrammarParser.ReturnStatementContext ctx) {
        AsgExpression returnValue = ctx.value.accept(context.asExpressionParser());
        if (!returnValue.getResultType().equals(context.getReturnType())) {
            returnValue = new AsgCastExpression(returnValue, context.getReturnType(), false);
        }
        return new AsgReturnStatement(context.getReturnType(), returnValue);
    }

    @Override
    protected AsgStatement aggregateResult(AsgStatement aggregate, AsgStatement nextResult) {
        ((AsgStatementList) aggregate).getStatements().add(nextResult);
        return aggregate;
    }

    @Override
    protected AsgStatement defaultResult() {
        return new AsgStatementList(new ArrayList<>());
    }
}
