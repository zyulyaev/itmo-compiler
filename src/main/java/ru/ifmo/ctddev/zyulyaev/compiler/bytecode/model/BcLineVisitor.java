package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model;

/**
 * @author zyulyaev
 * @since 31.05.2017
 */
public interface BcLineVisitor<T> {
    T visit(BcInstructionLine instructionLine);

    T visit(BcLabelLine labelLine);
}
