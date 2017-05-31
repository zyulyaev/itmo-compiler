package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value;

/**
 * @author zyulyaev
 * @since 31.05.2017
 */
public interface BcPointer extends BcValue {
    void set(BcValue value);

    BcValue get();

    BcPointer shift(int offset);

    int length();

    @Override
    default BcValueType getType() {
        return BcValueType.POINTER;
    }
}
