package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcJump implements BcInstruction {
    private final Condition condition;
    private final BcLine afterLine;

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public enum Condition {
        ALWAYS, IF_ZERO, IF_NOT_ZERO
    }
}
