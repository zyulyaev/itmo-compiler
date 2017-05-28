package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcIntValue implements BcValue {
    private final int value;

    @Override
    public BcValueType getType() {
        return BcValueType.INT;
    }
}
