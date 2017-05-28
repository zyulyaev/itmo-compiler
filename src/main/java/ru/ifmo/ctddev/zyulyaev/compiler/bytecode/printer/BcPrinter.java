package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.printer;

import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcArrayInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcBinOp;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstructionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJump;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcNullaryInstructions;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPush;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPushAddress;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class BcPrinter implements BcInstructionVisitor<Void>, Closeable {
    private final PrintWriter out;

    public BcPrinter(PrintWriter out) {
        this.out = out;
    }

    public void print(BcProgram program) {
        for (BcFunctionDefinition definition : program.getFunctions().values()) {
            out.printf("Definition of \"%s\"\n", definition.getFunction().getName());
            printBody(definition.getBody());
            out.println();
        }

        out.println("Main definition");
        printBody(program.getMain().getBody());
    }

    private void printBody(BcLine body) {
        for (BcLine line = body; line != null; line = line.getNext()) {
            line.getInstruction().accept(this);
        }
    }

    @Override
    public Void visit(BcArrayInit arrayInit) {
        out.println("ainit");
        return null;
    }

    @Override
    public Void visit(BcBinOp binOp) {
        out.printf("binop %s\n", binOp.getOperator());
        return null;
    }

    @Override
    public Void visit(BcJump jump) {
        switch (jump.getCondition()) {
        case ALWAYS:
            out.println("jmp");
            break;
        case IF_ZERO:
            out.println("jz");
            break;
        case IF_NOT_ZERO:
            out.println("jnz");
            break;
        }
        return null;
    }

    @Override
    public Void visit(BcPush push) {
        out.printf("push %d\n", push.getValue());
        return null;
    }

    @Override
    public Void visit(BcCall call) {
//        out.printf("call %s\n", call.getTarget().getName());
        out.println("call");
        return null;
    }

    @Override
    public Void visit(BcPushAddress pushAddress) {
        out.printf("pusha %s\n", pushAddress.getTarget().getName());
        return null;
    }

    @Override
    public Void visitNop(BcNullaryInstructions nop) {
        out.println("nop");
        return null;
    }

    @Override
    public Void visitLoad(BcNullaryInstructions load) {
        out.println("load");
        return null;
    }

    @Override
    public Void visitStore(BcNullaryInstructions store) {
        out.println("store");
        return null;
    }

    @Override
    public Void visitReturn(BcNullaryInstructions ret) {
        out.println("ret");
        return null;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
