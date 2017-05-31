package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public interface BcInstructionVisitor<T> {
    T visit(BcArrayInit arrayInit);

    T visit(BcStringInit stringInit);

    T visit(BcBinOp binOp);

    T visit(BcJump jump);

    T visit(BcPush push);

    T visit(BcCall call);

    T visit(BcPushAddress pushAddress);

    T visitNop(BcNullaryInstructions nop);

    T visitPop(BcNullaryInstructions pop);

    T visitLoad(BcNullaryInstructions load);

    T visitStore(BcNullaryInstructions store);

    T visitReturn(BcNullaryInstructions ret);

    T visit(BcUnset unset);

    T visitIndex(BcNullaryInstructions index);
}
