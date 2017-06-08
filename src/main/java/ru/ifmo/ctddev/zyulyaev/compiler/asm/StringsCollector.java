package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcArrayInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcBinOp;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCast;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcDataInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcIndexLoad;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcIndexStore;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstructionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJump;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJumpIfZero;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcLoad;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcMemberLoad;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcMemberStore;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcMethodCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcReturn;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcStore;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcStringInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcInstructionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLineVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zyulyaev
 * @since 07.06.2017
 */
class StringsCollector implements BcLineVisitor<Void>, BcInstructionVisitor<Void> {
    private final Set<String> strings = new HashSet<>();

    public Set<String> getStrings() {
        return strings;
    }

    @Override
    public Void visit(BcInstructionLine instructionLine) {
        instructionLine.getInstruction().accept(this);
        return null;
    }

    @Override
    public Void visit(BcLabelLine labelLine) {
        return null;
    }

    @Override
    public Void visit(BcArrayInit arrayInit) {
        return null;
    }

    @Override
    public Void visit(BcStringInit stringInit) {
        strings.add(stringInit.getValue());
        return null;
    }

    @Override
    public Void visit(BcDataInit dataInit) {
        return null;
    }

    @Override
    public Void visit(BcBinOp binOp) {
        return null;
    }

    @Override
    public Void visit(BcJump jump) {
        return null;
    }

    @Override
    public Void visit(BcJumpIfZero jump) {
        return null;
    }

    @Override
    public Void visit(BcCall call) {
        return null;
    }

    @Override
    public Void visit(BcMethodCall call) {
        return null;
    }

    @Override
    public Void visit(BcStore store) {
        return null;
    }

    @Override
    public Void visit(BcLoad load) {
        return null;
    }

    @Override
    public Void visit(BcIndexLoad indexLoad) {
        return null;
    }

    @Override
    public Void visit(BcIndexStore indexStore) {
        return null;
    }

    @Override
    public Void visit(BcMemberLoad memberLoad) {
        return null;
    }

    @Override
    public Void visit(BcMemberStore memberStore) {
        return null;
    }

    @Override
    public Void visit(BcReturn ret) {
        return null;
    }

    @Override
    public Void visit(BcCast cast) {
        return null;
    }
}
