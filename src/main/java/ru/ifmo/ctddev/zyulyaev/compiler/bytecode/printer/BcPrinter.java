package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.printer;

import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcArrayInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcBinOp;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstructionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJump;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcNullaryInstructions;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPush;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPushAddress;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcStringInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcUnset;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcInstructionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLineVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;

import java.io.PrintWriter;
import java.util.List;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class BcPrinter implements BcLineVisitor<String>, BcInstructionVisitor<String> {
    public void print(BcProgram program, PrintWriter out) {
        for (BcFunctionDefinition definition : program.getFunctions().values()) {
            out.printf("Definition of \"%s\"\n", definition.getFunction().getName());
            printBody(definition.getBody(), out);
            out.println();
        }

        out.println("Main definition");
        printBody(program.getMain().getBody(), out);
    }

    private void printBody(List<BcLine> body, PrintWriter out) {
        body.forEach(line -> out.println(line.accept(this)));
    }

    @Override
    public String visit(BcArrayInit arrayInit) {
        return "init_array " + arrayInit.getTarget().getName() + " " + arrayInit.getSize();
    }

    @Override
    public String visit(BcStringInit stringInit) {
        return "init_string " + stringInit.getTarget().getName() + " " + stringInit.getValue();
    }

    @Override
    public String visit(BcBinOp binOp) {
        return "binop " + binOp.getOperator();
    }

    @Override
    public String visit(BcJump jump) {
        String target = jump.getLabel().getName();
        switch (jump.getCondition()) {
        case ALWAYS: return "jmp " + target;
        case IF_ZERO: return "jz " + target;
        case IF_NOT_ZERO: return "jnz " + target;
        }
        throw new UnsupportedOperationException("Jump condition not supported: " + jump.getCondition());
    }

    @Override
    public String visit(BcPush push) {
        return "push " + push.getValue();
    }

    @Override
    public String visit(BcCall call) {
        return "call " + call.getTarget().getName();
    }

    @Override
    public String visit(BcPushAddress pushAddress) {
        return "pusha " + pushAddress.getTarget().getName();
    }

    @Override
    public String visitNop(BcNullaryInstructions nop) {
        return "nop";
    }

    @Override
    public String visitPop(BcNullaryInstructions pop) {
        return "pop";
    }

    @Override
    public String visitLoad(BcNullaryInstructions load) {
        return "load";
    }

    @Override
    public String visitStore(BcNullaryInstructions store) {
        return "store";
    }

    @Override
    public String visitReturn(BcNullaryInstructions ret) {
        return "ret";
    }

    @Override
    public String visit(BcUnset unset) {
        return "unset " + unset.getTarget().getName();
    }

    @Override
    public String visitIndex(BcNullaryInstructions index) {
        return "index";
    }

    @Override
    public String visit(BcInstructionLine instructionLine) {
        return "\t" + instructionLine.getInstruction().accept(this);
    }

    @Override
    public String visit(BcLabelLine labelLine) {
        return labelLine.getLabel().getName() + ":";
    }
}
