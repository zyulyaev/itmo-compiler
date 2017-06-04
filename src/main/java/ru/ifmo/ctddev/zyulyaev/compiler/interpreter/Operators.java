package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgBinaryOperator;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.RightValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class Operators {
    static Value apply(RightValue left, RightValue right, AsgBinaryOperator operator) {
        return applyInt(left.asInt(), right.asInt(), operator);
    }

    private static Value applyInt(IntValue left, IntValue right, AsgBinaryOperator operator) {
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
    
    private static Value intValue(int value) {
        return new IntValue(value);
    }
    
    private static Value boolValue(boolean value) {
        return new IntValue(value ? 1 : 0);
    }
}
