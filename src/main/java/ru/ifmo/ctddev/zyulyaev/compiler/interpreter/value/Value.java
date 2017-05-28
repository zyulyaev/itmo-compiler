package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public interface Value {
    ValueType getType();

    default StringValue asString() {
        return (StringValue) this;
    }

    default IntValue asInt() {
        return (IntValue) this;
    }

    default ArrayValue asArray() {
        return (ArrayValue) this;
    }
}