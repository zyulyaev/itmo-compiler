package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcArrayInit implements BcInstruction {
    private final int size;

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
