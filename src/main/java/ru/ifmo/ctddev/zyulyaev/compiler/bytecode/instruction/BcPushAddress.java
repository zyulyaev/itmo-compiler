package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcPushAddress implements BcInstruction {
    private final BcVariable target;

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
