package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 31.05.2017
 */
@Data
class IntValue implements Value {
    private final int value;

    @Override
    public ValueType getType() {
        return ValueType.INT;
    }
}
