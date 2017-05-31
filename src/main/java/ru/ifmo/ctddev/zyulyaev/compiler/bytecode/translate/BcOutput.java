package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcInstructionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcOutput {
    private final List<BcLine> lines = new ArrayList<>();

    public BcLine write(BcInstruction instruction) {
        BcLine line = new BcInstructionLine(instruction);
        lines.add(line);
        return line;
    }

    public BcLine write(BcLabel label) {
        BcLine line = new BcLabelLine(label);
        lines.add(line);
        return line;
    }

    public List<BcLine> getLines() {
        return lines;
    }
}
