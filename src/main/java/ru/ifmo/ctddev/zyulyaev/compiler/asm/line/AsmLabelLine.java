package ru.ifmo.ctddev.zyulyaev.compiler.asm.line;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmSymbol;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class AsmLabelLine implements AsmLine {
    private final AsmSymbol symbol;

    @Override
    public String print() {
        return symbol.print() + ":";
    }
}
