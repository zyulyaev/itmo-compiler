package ru.ifmo.ctddev.zyulyaev.compiler.asm.operand;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
@Data
public class AsmSymbol implements AsmOperand {
    private final String value;

    @Override
    public String print() {
        return value;
    }
}
