package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcArrayPtrValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcIntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcValueType;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.BinaryOperator;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
class Operators {
    static BcValue apply(BcValue left, BcValue right, BinaryOperator operator) {
        BcValueType leftType = left.getType();
        BcValueType rightType = right.getType();
        if (leftType == BcValueType.INT && rightType == BcValueType.INT) {
            return applyInt(left.asInt(), right.asInt(), operator);
        } else if (leftType == BcValueType.PTR && rightType == BcValueType.INT) {
            return applyPtr(left.asPtr(), right.asInt(), operator);
        } else {
            throw new UnsupportedOperationException("Operations on " + leftType + " and " + rightType + " are not supported");
        }
    }

    private static BcValue applyPtr(BcArrayPtrValue left, BcIntValue right, BinaryOperator operator) {
        switch (operator) {
        case ADD:
            return new BcArrayPtrValue(left.getValues(), left.getIndex() + right.getValue());
        }
        throw new UnsupportedOperationException("Operator " + operator + " is not supported on PTR and INT");
    }


    private static BcValue applyInt(BcIntValue left, BcIntValue right, BinaryOperator operator) {
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
        case WAT: return boolValue(lValue != 0 || rValue != 0);
        }
        throw new UnsupportedOperationException("Operator " + operator + " is not supported on INT and INT");
    }

    private static BcIntValue intValue(int value) {
        return new BcIntValue(value);
    }

    private static BcIntValue boolValue(boolean value) {
        return new BcIntValue(value ? 1 : 0);
    }
}
