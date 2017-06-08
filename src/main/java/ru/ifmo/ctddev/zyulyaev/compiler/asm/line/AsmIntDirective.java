package ru.ifmo.ctddev.zyulyaev.compiler.asm.line;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmSymbol;

/**
 * @author zyulyaev
 * @since 07.06.2017
 */
@Data
public class AsmIntDirective implements AsmLine {
    private final AsmSymbol symbol;

    @Override
    public String print() {
        return ".int " + symbol.print();
    }
}
