package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmInstuctionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLine;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zyulyaev
 * @since 30.05.2017
 */
public class AsmOutput {
    private final List<AsmLine> lines = new ArrayList<>();

    public void write(AsmLine line) {
        lines.add(line);
    }

    public void write(AsmInstruction instruction) {
        write(new AsmInstuctionLine(instruction));
    }

    public List<AsmLine> getLines() {
        return lines;
    }
}
