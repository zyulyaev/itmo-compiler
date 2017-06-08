package ru.ifmo.ctddev.zyulyaev.compiler.asm.operand;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public interface AsmOperand {
    String print();

    default boolean isRegister() {
        return false;
    }

    default boolean isPointer() {
        return false;
    }

    default boolean isImmediate() {
        return false;
    }

    default boolean isSymbol() {
        return false;
    }
}
