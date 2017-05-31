package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcScalar;
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
        if (leftType == BcValueType.SCALAR && rightType == BcValueType.SCALAR) {
            return applyScalar(left.asScalar(), right.asScalar(), operator);
        } else {
            throw new UnsupportedOperationException("Operations on " + leftType + " and " + rightType + " are not supported");
        }
    }

    private static BcValue applyScalar(BcScalar left, BcScalar right, BinaryOperator operator) {
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
        throw new UnsupportedOperationException("Operator " + operator + " is not supported on scalars");
    }

    private static BcScalar intValue(int value) {
        return new BcScalar(value);
    }

    private static BcScalar boolValue(boolean value) {
        return new BcScalar(value ? 1 : 0);
    }
}
