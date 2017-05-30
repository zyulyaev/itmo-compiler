package ru.ifmo.ctddev.zyulyaev.compiler.asm.line;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmInstruction;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class AsmInstuctionLine implements AsmLine {
    private final AsmInstruction instruction;

    @Override
    public String print() {
        return "\t" + instruction.print();
    }
}
