package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.GrammarBaseVisitor;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLeftValueExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgAssignment;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgExpressionStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgForStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgIfStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgRepeatStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgReturnStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementList;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgVariableAssignment;
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
        return new AsgVariableAssignment(variable, expression);
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
        AsgStatement positive = ctx.positive.accept(context.createChild().asStatementParser());
        AsgStatement negative = null;
        if (ctx.negative != null) {
            negative = ctx.negative.accept(context.createChild().asStatementParser());
        }
        return new AsgIfStatement(condition, positive, negative);
    }

    @Override
    public AsgStatement visitElif(GrammarParser.ElifContext ctx) {
        AsgExpression condition = ctx.condition.accept(context.asExpressionParser());
        AsgStatement positive = ctx.positive.accept(context.createChild().asStatementParser());
        AsgStatement negative = null;
        if (ctx.negative != null) {
            negative = ctx.negative.accept(context.createChild().asStatementParser());
        }
        return new AsgIfStatement(condition, positive, negative);
    }

    @Override
    public AsgStatement visitForStatement(GrammarParser.ForStatementContext ctx) {
        Context forContext = context.createChild();
        AsgStatement initialization = ctx.initializaion.accept(forContext.asStatementParser());
        AsgExpression termination = ctx.termination.accept(forContext.asExpressionParser());
        AsgStatement increment = ctx.increment.accept(forContext.asStatementParser());
        AsgStatement body = ctx.body.accept(forContext.asStatementParser());
        return new AsgForStatement(initialization, termination, increment, body);
    }

    @Override
    public AsgStatement visitWhileStatement(GrammarParser.WhileStatementContext ctx) {
        AsgExpression condition = ctx.condition.accept(context.asExpressionParser());
        AsgStatement body = ctx.body.accept(context.createChild().asStatementParser());
        return new AsgWhileStatement(condition, body);
    }

    @Override
    public AsgStatement visitRepeatStatement(GrammarParser.RepeatStatementContext ctx) {
        Context repeatContext = context.createChild();
        AsgStatement body = ctx.body.accept(repeatContext.asStatementParser());
        AsgExpression condition = ctx.condition.accept(repeatContext.asExpressionParser());
        return new AsgRepeatStatement(condition, body);
    }

    @Override
    public AsgStatement visitReturnStatement(GrammarParser.ReturnStatementContext ctx) {
        return new AsgReturnStatement(ctx.value.accept(context.asExpressionParser()));
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
