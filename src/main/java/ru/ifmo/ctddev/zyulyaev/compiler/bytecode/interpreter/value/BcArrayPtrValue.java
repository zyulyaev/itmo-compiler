package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcArrayPtrValue implements BcValue {
    private final BcValue[] values;
    private final int index;

    @Override
    public BcValueType getType() {
        return BcValueType.PTR;
    }
}
