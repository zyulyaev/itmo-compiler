package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import com.google.common.collect.ImmutableMap;
import ru.ifmo.ctddev.zyulyaev.GrammarBaseVisitor;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgBinaryOperator;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgArrayExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgBinaryExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgCastExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgDataExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgFunctionCallExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgIndexExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgMemberAccessExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgMethodCallExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgVariableExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class ExpressionParser extends GrammarBaseVisitor<AsgExpression> {
    private static final Map<String, AsgBinaryOperator> OPERATOR_BY_TOKEN = ImmutableMap.<String, AsgBinaryOperator>builder()
        .put("*", AsgBinaryOperator.MUL)
        .put("/", AsgBinaryOperator.DIV)
        .put("%", AsgBinaryOperator.MOD)

        .put("+", AsgBinaryOperator.ADD)
        .put("-", AsgBinaryOperator.SUB)

        .put(">", AsgBinaryOperator.GT)
        .put(">=", AsgBinaryOperator.GTE)
        .put("<", AsgBinaryOperator.LT)
        .put("<=", AsgBinaryOperator.LTE)
        .put("==", AsgBinaryOperator.EQ)
        .put("!=", AsgBinaryOperator.NEQ)

        .put("&&", AsgBinaryOperator.AND)
        .put("!!", AsgBinaryOperator.OR)

        .build();

    private final Context context;

    ExpressionParser(Context context) {
        this.context = context;
    }

    @Override
    public AsgExpression visitBinExpr(GrammarParser.BinExprContext ctx) {
        AsgExpression left = ctx.left.accept(this);
        AsgExpression right = ctx.right.accept(this);
        AsgBinaryOperator operator = OPERATOR_BY_TOKEN.get(ctx.op.getText());
        return new AsgBinaryExpression(left, right, operator);
    }

    @Override
    public AsgExpression visitIndexLeftValue1(GrammarParser.IndexLeftValue1Context ctx) {
        AsgExpression array = ctx.array.accept(this);
        AsgExpression index = ctx.index.accept(this);
        return new AsgIndexExpression(array, index);
    }

    @Override
    public AsgExpression visitIndexLeftValue2(GrammarParser.IndexLeftValue2Context ctx) {
        AsgExpression array = ctx.array.accept(this);
        AsgExpression index = ctx.index.accept(this);
        return new AsgIndexExpression(array, index);
    }

    @Override
    public AsgExpression visitMemberAccessLeftValue1(GrammarParser.MemberAccessLeftValue1Context ctx) {
        AsgExpression object = ctx.object.accept(this);
        String member = ctx.member.getText();
        return new AsgMemberAccessExpression(object, member);
    }

    @Override
    public AsgExpression visitMemberAccessLeftValue2(GrammarParser.MemberAccessLeftValue2Context ctx) {
        AsgExpression object = ctx.object.accept(this);
        String member = ctx.member.getText();
        return new AsgMemberAccessExpression(object, member);
    }

    @Override
    public AsgExpression visitParensTerm(GrammarParser.ParensTermContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public AsgExpression visitFunctionCallTerm(GrammarParser.FunctionCallTermContext ctx) {
        List<AsgExpression> arguments = ctx.args.argument().stream()
            .map(arg -> arg.accept(this))
            .collect(Collectors.toList());
        List<AsgType> argumentTypes = arguments.stream().map(AsgExpression::getResultType).collect(Collectors.toList());
        AsgFunction function = context.resolveFunction(ctx.name.getText(), argumentTypes);
        castArguments(arguments, function.getParameterTypes());
        return new AsgFunctionCallExpression(function, arguments);
    }

    @Override
    public AsgExpression visitMethodCallTerm(GrammarParser.MethodCallTermContext ctx) {
        AsgExpression object = ctx.object.accept(this);
        AsgType objectType = object.getResultType();
        String methodName = ctx.method.getText();
        AsgMethod method = context.resolveMethod(objectType, methodName);
        List<AsgExpression> arguments = ctx.args.argument().stream()
            .map(arg -> arg.accept(this))
            .collect(Collectors.toList());
        castArguments(arguments, method.getParameterTypes());
        return new AsgMethodCallExpression(object, method, arguments);
    }

    private void castArguments(List<AsgExpression> arguments, List<AsgType> parameterTypes) {
        for (int i = 0; i < arguments.size(); i++) {
            AsgType parameterType = parameterTypes.get(i);
            arguments.set(i, castIfNeeded(parameterType, arguments.get(i)));
        }
    }

    @Override
    public AsgExpression visitLiteralTerm(GrammarParser.LiteralTermContext ctx) {
        return ctx.literal().accept(new LiteralParser());
    }

    @Override
    public AsgExpression visitIdTerm(GrammarParser.IdTermContext ctx) {
        return new AsgVariableExpression(context.resolveVariable(ctx.id().getText()));
    }

    @Override
    public AsgExpression visitArrayExpr(GrammarParser.ArrayExprContext ctx) {
        List<AsgExpression> values = ctx.arguments().argument().stream()
            .map(arg -> arg.accept(this))
            .collect(Collectors.toList());
        return new AsgArrayExpression(values);
    }

    @Override
    public AsgExpression visitCastExpr(GrammarParser.CastExprContext ctx) {
        AsgExpression expression = ctx.expression().accept(this);
        AsgType target = ctx.type().accept(context.asTypeParser());
        return new AsgCastExpression(expression, target, true);
    }

    @Override
    public AsgExpression visitDataExpr(GrammarParser.DataExprContext ctx) {
        AsgDataType dataType = (AsgDataType) context.resolveType(ctx.dataExpression().dataType.getText());
        Map<AsgDataType.Field, AsgExpression> values = new HashMap<>();
        for (GrammarParser.FieldExpressionContext fieldCtx : ctx.dataExpression().fieldExpression()) {
            AsgDataType.Field field = dataType.getField(fieldCtx.name.getText());
            AsgExpression value = castIfNeeded(field.getType(), fieldCtx.value.accept(this));
            values.put(field, value);
        }
        return new AsgDataExpression(dataType, values);
    }

    private static AsgExpression castIfNeeded(AsgType targetType, AsgExpression expression) {
        if (!expression.getResultType().equals(targetType)) {
            return new AsgCastExpression(expression, targetType, false);
        } else {
            return expression;
        }
    }
}
