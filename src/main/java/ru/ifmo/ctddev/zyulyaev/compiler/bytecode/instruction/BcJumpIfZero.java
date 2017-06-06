package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcJumpIfZero implements BcInstruction {
    private final BcValue condition;
    private final BcLabel label;

    @Override
    public AsgType getResultType() {
        return AsgPredefinedType.NONE;
    }

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
