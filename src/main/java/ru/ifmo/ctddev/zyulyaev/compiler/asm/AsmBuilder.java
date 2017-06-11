package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmBinary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmUnary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmInstuctionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmOperand;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zyulyaev
 * @since 30.05.2017
 */
public class AsmBuilder {
    private final List<AsmLine> lines = new ArrayList<>();

    public AsmBuilder write(AsmLine line) {
        lines.add(line);
        return this;
    }

    public AsmBuilder write(AsmInstruction instruction) {
        return write(new AsmInstuctionLine(instruction));
    }

    public AsmBuilder write(AsmBinary operator, AsmOperand dest, AsmOperand src) {
        return write(operator.create(dest, src));
    }

    public AsmBuilder write(AsmUnary operator, AsmOperand operand) {
        return write(operator.create(operand));
    }

    public AsmBuilder write(AsmSymbol label) {
        return write(new AsmLabelLine(label));
    }

    /**
     * If source is already a register, returns itself. Otherwise moves value from source to specified destination
     * and returns it.
     */
    public AsmRegister loadAsRegister(AsmOperand value, AsmRegister register) {
        if (value.isRegister()) {
            return (AsmRegister) value;
        } else {
            write(AsmBinary.MOV, register, value);
            return register;
        }
    }

    public AsmOperand loadAsValue(AsmOperand value, AsmRegister register) {
        return value.isPointer() ? loadAsRegister(value, register) : value;
    }

    public void move(AsmOperand dest, AsmOperand src, AsmRegister tmp) {
        if (dest.isPointer() && src.isPointer()) {
            write(AsmBinary.MOV, tmp, src);
            src = tmp;
        }
        write(AsmBinary.MOV, dest, src);
    }

    public void append(AsmBuilder output) {
        lines.addAll(output.lines);
    }

    public List<AsmLine> getLines() {
        return lines;
    }
}
