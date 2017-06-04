package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class ArrayValue implements RightValue {
    private final RightValue[] values;

    public void set(int index, RightValue value) {
        this.values[index] = value;
    }

    public RightValue get(int index) {
        return this.values[index];
    }

    public int length() {
        return values.length;
    }
}
