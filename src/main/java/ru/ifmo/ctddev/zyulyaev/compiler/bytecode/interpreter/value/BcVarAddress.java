package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value;

import lombok.Data;

import java.util.List;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
@Data
public class BcVarAddress implements BcValue {
    private final List<BcValue> stack;
    private final int index;

    @Override
    public BcValueType getType() {
        return BcValueType.VAR;
    }
}
