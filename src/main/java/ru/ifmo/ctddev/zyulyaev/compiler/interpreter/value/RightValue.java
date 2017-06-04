package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
public interface RightValue extends Value {
    @Override
    default LeftValue asLeftValue() {
        throw new UnsupportedOperationException("Right value cannot be converted to left value");
    }

    @Override
    default RightValue asRightValue() {
        return this;
    }

    default StringValue asString() {
        return (StringValue) this;
    }

    default IntValue asInt() {
        return (IntValue) this;
    }

    default ArrayValue asArray() {
        return (ArrayValue) this;
    }

    default DataTypeValue asDataType() {
        return (DataTypeValue) this;
    }
}
