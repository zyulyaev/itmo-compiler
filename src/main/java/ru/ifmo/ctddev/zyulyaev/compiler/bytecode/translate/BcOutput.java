package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public interface BcOutput {
    BcLine write(BcInstruction instruction);

    BcDummy dummy();

    BcLine getStart();
}
