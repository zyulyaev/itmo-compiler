package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgBinaryOperator;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
class Operators {
    static Value apply(Value left, Value right, AsgBinaryOperator operator) {
        ValueType leftType = left.getType();
        ValueType rightType = right.getType();
        if (leftType == ValueType.INT && rightType == ValueType.INT) {
            return applyScalar(left.asInt(), right.asInt(), operator);
        } else {
            throw new UnsupportedOperationException("Operations on " + leftType + " and " + rightType + " are not supported");
        }
    }

    private static Value applyScalar(IntValue left, IntValue right, AsgBinaryOperator operator) {
        int lValue = left.getValue();
        int rValue = right.getValue();
        switch (operator) {
        case MUL: return intValue(lValue * rValue);
        case DIV: return intValue(lValue / rValue);
        case MOD: return intValue(lValue % rValue);

        case ADD: return intValue(lValue + rValue);
        case SUB: return intValue(lValue - rValue);

        case LT: return boolValue(lValue < rValue);
        case LTE: return boolValue(lValue <= rValue);
        case GT: return boolValue(lValue > rValue);
        case GTE: return boolValue(lValue >= rValue);
        case EQ: return boolValue(lValue == rValue);
        case NEQ: return boolValue(lValue != rValue);

        case AND: return boolValue(lValue != 0 && rValue != 0);
        case OR: return boolValue(lValue != 0 || rValue != 0);
        }
        throw new UnsupportedOperationException("Operator " + operator + " is not supported on scalars");
    }

    private static IntValue intValue(int value) {
        return new IntValue(value);
    }

    private static IntValue boolValue(boolean value) {
        return new IntValue(value ? 1 : 0);
    }
}
