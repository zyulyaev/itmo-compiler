package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 06.06.2017
 */
@Data
class ArrayValue implements Value {
    private final Value[] values;

    Value get(int index) {
        return values[index];
    }

    void set(int index, Value value) {
        values[index] = value;
    }

    int length() {
        return values.length;
    }

    @Override
    public ValueType getType() {
        return ValueType.ARRAY;
    }
}
