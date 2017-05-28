package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public interface BcInstruction {
    <T> T accept(BcInstructionVisitor<T> visitor);
}
