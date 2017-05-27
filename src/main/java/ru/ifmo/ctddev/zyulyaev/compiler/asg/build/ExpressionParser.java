package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import com.google.common.collect.ImmutableMap;
import ru.ifmo.ctddev.zyulyaev.GrammarBaseVisitor;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgBinaryExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgFunctionCallExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgVariableExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.BinaryOperator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class ExpressionParser extends GrammarBaseVisitor<AsgExpression> {
    private static final Map<String, BinaryOperator> OPERATOR_BY_TOKEN = ImmutableMap.<String, BinaryOperator>builder()
        .put("*", BinaryOperator.MUL)
        .put("/", BinaryOperator.DIV)
        .put("%", BinaryOperator.MOD)

        .put("+", BinaryOperator.ADD)
        .put("-", BinaryOperator.SUB)

        .put(">", BinaryOperator.GT)
        .put(">=", BinaryOperator.GTE)
        .put("<", BinaryOperator.LT)
        .put("<=", BinaryOperator.LTE)
        .put("==", BinaryOperator.EQ)
        .put("!=", BinaryOperator.NEQ)

        .put("&&", BinaryOperator.AND)
        .put("||", BinaryOperator.OR)

        .build();

    private final Context context;

    ExpressionParser(Context context) {
        this.context = context;
    }

    @Override
    public AsgExpression visitBinExpr(GrammarParser.BinExprContext ctx) {
        AsgExpression left = ctx.left.accept(this);
        AsgExpression right = ctx.right.accept(this);
        BinaryOperator operator = OPERATOR_BY_TOKEN.get(ctx.op.getText());
        return new AsgBinaryExpression(left, right, operator);
    }

    @Override
    public AsgExpression visitFunctionCall(GrammarParser.FunctionCallContext ctx) {
        AsgFunction function = context.resolveFunction(ctx.name.getText());
        List<AsgExpression> arguments = ctx.args.argument().stream()
            .map(arg -> arg.accept(context.asExpressionParser()))
            .collect(Collectors.toList());
        return new AsgFunctionCallExpression(function, arguments);
    }

    @Override
    public AsgExpression visitIdExpr(GrammarParser.IdExprContext ctx) {
        AsgVariable variable = context.resolveVariable(ctx.getText());
        return new AsgVariableExpression(variable);
    }

    @Override
    public AsgExpression visitLiteralExpr(GrammarParser.LiteralExprContext ctx) {
        return ctx.literal().accept(new LiteralParser());
    }

    @Override
    public AsgExpression visitParensExpr(GrammarParser.ParensExprContext ctx) {
        return ctx.expression().accept(this);
    }
}
