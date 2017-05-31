package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public interface BcValue {
    BcValueType getType();

    default BcScalar asScalar() {
        return (BcScalar) this;
    }

    default BcPointer asPtr() {
        return (BcPointer) this;
    }
}
