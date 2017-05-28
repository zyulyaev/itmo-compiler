package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model;

import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstruction;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public interface BcLine {
    BcLine getNext();

    BcInstruction getInstruction();
}
