package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 31.05.2017
 */
@Data
public class BcScalar implements BcValue {
    private final int value;

    @Override
    public BcValueType getType() {
        return BcValueType.SCALAR;
    }
}
