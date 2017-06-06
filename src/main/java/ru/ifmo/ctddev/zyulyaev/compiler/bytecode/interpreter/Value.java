package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
interface Value {
    ValueType getType();

    default IntValue asInt() {
        return (IntValue) this;
    }

    default ArrayValue asArray() {
        return (ArrayValue) this;
    }

    default StringValue asString() {
        return (StringValue) this;
    }

    default DataValue asData() {
        return (DataValue) this;
    }
}
