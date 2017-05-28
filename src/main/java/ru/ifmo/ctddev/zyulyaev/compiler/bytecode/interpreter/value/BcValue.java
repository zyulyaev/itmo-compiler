package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public interface BcValue {
    BcValueType getType();

    default BcIntValue asInt() {
        return (BcIntValue) this;
    }

    default BcArrayPtrValue asPtr() {
        return (BcArrayPtrValue) this;
    }

    default BcVarAddress asVar() {
        return (BcVarAddress) this;
    }
}
