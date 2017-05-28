package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class ArrayValue implements Value {
    private final Value[] values;

    public ArrayValue(Value[] values) {
        this.values = values;
    }

    public void set(int index, Value value) {
        this.values[index] = value;
    }

    public Value get(int index) {
        return this.values[index];
    }

    public int length() {
        return values.length;
    }

    @Override
    public ValueType getType() {
        return ValueType.ARRAY;
    }
}
