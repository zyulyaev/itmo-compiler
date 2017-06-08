package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmInstuctionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmSymbol;

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

    public void write(AsmInstruction... instructions) {
        for (AsmInstruction instruction : instructions) {
            write(instruction);
        }
    }

    public void write(AsmSymbol label) {
        write(new AsmLabelLine(label));
    }

    public void append(AsmOutput output) {
        lines.addAll(output.lines);
    }

    public List<AsmLine> getLines() {
        return lines;
    }
}
