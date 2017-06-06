package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;

/**
 * @author zyulyaev
 * @since 05.06.2017
 */
@Data
public class BcStore implements BcInstruction {
    private final AsgVariable variable;
    private final BcValue value;

    @Override
    public AsgType getResultType() {
        return AsgPredefinedType.NONE;
    }

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
