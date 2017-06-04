package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgArrayExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgBinaryExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgCastExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgDataExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpressionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgFunctionCallExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgIndexExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLiteralExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgMemberAccessExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgMethodCallExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgVariableExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.ArrayIndexValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.ArrayValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.DataTypeValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.MemberValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.NoneValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.RightValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.StringValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class ExpressionInterpreter implements AsgExpressionVisitor<Value> {
    private final InterpreterContext context;

    ExpressionInterpreter(InterpreterContext context) {
        this.context = context;
    }

    @Override
    public Value visit(AsgLiteralExpression<?> literal) {
        switch (literal.getType()) {
        case INT:
            return new IntValue((Integer) literal.getValue());
        case STRING:
            return new StringValue(((String) literal.getValue()).toCharArray());
        case NONE:
            return NoneValue.INSTANCE;
        }
        throw new IllegalArgumentException("Unexpected literal type: " + literal.getType());
    }

    @Override
    public Value visit(AsgBinaryExpression binaryExpression) {
        Value left = binaryExpression.getLeft().accept(this);
        Value right = binaryExpression.getRight().accept(this);
        return Operators.apply(left.asRightValue(), right.asRightValue(), binaryExpression.getOperator());
    }

    @Override
    public Value visit(AsgFunctionCallExpression functionCall) {
        AsgFunction function = functionCall.getFunction();
        List<RightValue> arguments = functionCall.getArguments().stream()
            .map(arg -> arg.accept(this).asRightValue())
            .collect(Collectors.toList());
        return context.callFunction(function, arguments);
    }

    @Override
    public Value visit(AsgMethodCallExpression methodCall) {
        DataTypeValue object = methodCall.getObject().accept(this).asRightValue().asDataType();
        List<RightValue> arguments = methodCall.getArguments().stream()
            .map(arg -> arg.accept(this).asRightValue())
            .collect(Collectors.toList());
        return context.callMethod(object, methodCall.getMethod(), arguments);
    }

    @Override
    public Value visit(AsgArrayExpression arrayExpression) {
        RightValue[] values = arrayExpression.getValues().stream()
            .map(value -> value.accept(this).asRightValue())
            .toArray(RightValue[]::new);
        return new ArrayValue(values);
    }

    @Override
    public Value visit(AsgIndexExpression indexExpression) {
        ArrayValue array = indexExpression.getArray().accept(this).asRightValue().asArray();
        IntValue index = indexExpression.getIndex().accept(this).asRightValue().asInt();
        return new ArrayIndexValue(array, index.getValue());
    }

    @Override
    public Value visit(AsgMemberAccessExpression memberAccessExpression) {
        DataTypeValue object = memberAccessExpression.getObject().accept(this).asRightValue().asDataType();
        AsgDataType.Field field = memberAccessExpression.getField();
        return new MemberValue(object, field);
    }

    @Override
    public Value visit(AsgVariableExpression variableExpression) {
        return context.getVariableValue(variableExpression.getVariable());
    }

    @Override
    public Value visit(AsgCastExpression castExpression) {
        return castExpression.getExpression().accept(this);
    }

    @Override
    public Value visit(AsgDataExpression dataExpression) {
        Map<AsgDataType.Field, RightValue> values = dataExpression.getValues().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().accept(this).asRightValue()
            ));
        return new DataTypeValue(dataExpression.getType(), values);
    }
}
