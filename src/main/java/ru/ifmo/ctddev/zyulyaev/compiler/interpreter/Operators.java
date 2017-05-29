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
        int lValue = left.getValue();
        int rValue = right.getValue();
        switch (operator) {
        case MUL: return intValue(lValue * rValue);
        case DIV: return intValue(lValue / rValue);
        case MOD: return intValue(lValue % rValue);

        case ADD: return intValue(lValue + rValue);
        case SUB: return intValue(lValue - rValue);

        case GT: return boolValue(lValue > rValue);
        case LT: return boolValue(lValue < rValue);
        case GTE: return boolValue(lValue >= rValue);
        case LTE: return boolValue(lValue <= rValue);
        case EQ: return boolValue(lValue == rValue);
        case NEQ: return boolValue(lValue != rValue);

        case AND: return boolValue(lValue != 0 && rValue != 0);
        case OR: return boolValue(lValue != 0 || rValue != 0);
        case WAT: return boolValue(lValue != 0 || rValue != 0);
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
