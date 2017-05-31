package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;

/**
 * @author zyulyaev
 * @since 30.05.2017
 */
@Data
public class BcStringInit implements BcInstruction {
    private final BcVariable target;
    private final String value;

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
