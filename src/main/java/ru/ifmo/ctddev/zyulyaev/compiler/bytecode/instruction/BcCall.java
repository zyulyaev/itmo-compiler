package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunction;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcCall implements BcInstruction {
    private final BcFunction target;

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
