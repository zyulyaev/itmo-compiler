package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.StringValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.ValueType;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.BinaryOperator;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class Operators {
    public static Value apply(Value left, Value right, BinaryOperator operator) {
        ValueType leftType = left.getType();
        ValueType rightType = right.getType();
        if (leftType != rightType) {
            throw new IllegalArgumentException("Cannot apply operator on different types: " + leftType + " and " + rightType);
        }
        switch (leftType) {
        case INT: return applyInt((IntValue) left, (IntValue) right, operator);
        case STRING: return applyString((StringValue) left, (StringValue) right, operator);
        }

        throw new UnsupportedOperationException("Unexpected type: " + leftType);
    }

    private static Value applyInt(IntValue left, IntValue right, BinaryOperator operator) {
        switch (operator) {
        case MUL: return intValue(left.getValue() * right.getValue());
        case DIV: return intValue(left.getValue() / right.getValue());
        case MOD: return intValue(left.getValue() % right.getValue());

        case ADD: return intValue(left.getValue() + right.getValue());
        case SUB: return intValue(left.getValue() - right.getValue());

        case GT: return boolValue(left.getValue() > right.getValue());
        case LT: return boolValue(left.getValue() < right.getValue());
        case GTE: return boolValue(left.getValue() >= right.getValue());
        case LTE: return boolValue(left.getValue() <= right.getValue());
        case EQ: return boolValue(left.getValue() == right.getValue());
        case NEQ: return boolValue(left.getValue() != right.getValue());

        case AND: return boolValue(left.getValue() != 0 && right.getValue() != 0);
        case OR: return boolValue(left.getValue() != 0 || right.getValue() != 0);
        }

        throw new UnsupportedOperationException("Operator not supported on int type: " + operator);
    }

    private static Value applyString(StringValue left, StringValue right, BinaryOperator operator) {
        switch (operator) {
        case ADD: return new StringValue(left.getValue().concat(right.getValue()));
        }

        throw new UnsupportedOperationException("Operator not supported on string type: " + operator);
    }
    
    private static Value intValue(int value) {
        return new IntValue(value);
    }
    
    private static Value boolValue(boolean value) {
        return new IntValue(value ? 1 : 0);
    }
}
