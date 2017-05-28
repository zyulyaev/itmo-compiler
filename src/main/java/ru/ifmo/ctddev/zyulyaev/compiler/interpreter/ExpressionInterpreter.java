package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgArrayExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgBinaryExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpressionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgFunctionCallExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLiteralExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLeftValueExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.ArrayValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.StringValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class ExpressionInterpreter implements AsgExpressionVisitor<Value> {
    private final InterpreterContext context;

    public ExpressionInterpreter(InterpreterContext context) {
        this.context = context;
    }

    @Override
    public Value visit(AsgLiteralExpression<?> literal) {
        switch (literal.getType()) {
        case INT: return new IntValue((Integer) literal.getValue());
        case STRING: return new StringValue((String) literal.getValue());
        case NULL: return null;
        }
        throw new IllegalArgumentException("Unexpected literal type: " + literal.getType());
    }

    @Override
    public Value visit(AsgLeftValueExpression leftValue) {
        return context.getVariableValue(interpretLeftValue(leftValue));
    }

    @Override
    public Value visit(AsgBinaryExpression binaryExpression) {
        Value left = binaryExpression.getLeft().accept(this);
        Value right = binaryExpression.getRight().accept(this);
        return Operators.apply(left, right, binaryExpression.getOperator());
    }

    @Override
    public Value visit(AsgFunctionCallExpression functionCall) {
        AsgFunction function = functionCall.getFunction();
        List<Value> arguments = functionCall.getArguments().stream()
            .map(arg -> arg.accept(this))
            .collect(Collectors.toList());
        return context.callFunction(function, arguments);
    }

    @Override
    public Value visit(AsgArrayExpression arrayExpression) {
        Value[] values = arrayExpression.getValues().stream()
            .map(value -> value.accept(this))
            .toArray(Value[]::new);
        return new ArrayValue(values);
    }

    public LeftValue interpretLeftValue(AsgLeftValueExpression leftValue) {
        List<IntValue> indexes = leftValue.getIndexes().stream()
            .map(index -> (IntValue) index.accept(this))
            .collect(Collectors.toList());
        return new LeftValue(leftValue.getVariable(), indexes);
    }
}
