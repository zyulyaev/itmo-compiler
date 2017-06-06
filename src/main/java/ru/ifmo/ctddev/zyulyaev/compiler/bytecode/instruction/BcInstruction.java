package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public interface BcInstruction {
    AsgType getResultType();

    <T> T accept(BcInstructionVisitor<T> visitor);
}
