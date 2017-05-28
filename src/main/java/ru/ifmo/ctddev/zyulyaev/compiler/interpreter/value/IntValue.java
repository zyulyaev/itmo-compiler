package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class IntValue implements Value {
    private final int value;

    public IntValue(int value) {
        this.value = value;
    }

    @Override
    public ValueType getType() {
        return ValueType.INT;
    }
}
