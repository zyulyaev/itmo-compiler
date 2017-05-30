package ru.ifmo.ctddev.zyulyaev.compiler.asm.operand;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
@Data
public class AsmImmediate implements AsmOperand {
    private final int value;

    @Override
    public String print() {
        return "$" + value;
    }
}
