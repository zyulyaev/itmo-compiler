package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public interface BcInstructionVisitor<T> {
    T visit(BcArrayInit arrayInit);

    T visit(BcStringInit stringInit);

    T visit(BcDataInit dataInit);

    T visit(BcBinOp binOp);

    T visit(BcJump jump);

    T visit(BcJumpIfZero jump);

    T visit(BcCall call);

    T visit(BcMethodCall call);

    T visit(BcStore store);

    T visit(BcLoad load);

    T visit(BcIndexLoad indexLoad);

    T visit(BcIndexStore indexStore);

    T visit(BcMemberLoad memberLoad);

    T visit(BcMemberStore memberStore);

    T visit(BcReturn ret);

    T visit(BcCast cast);

    T visit(BcUnset unset);
}
