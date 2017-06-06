package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgTypeUtils;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;

/**
 * @author zyulyaev
 * @since 05.06.2017
 */
@Data
public class BcIndexLoad implements BcInstruction {
    private final BcRegister array;
    private final BcValue index;

    @Override
    public AsgType getResultType() {
        return AsgTypeUtils.getCompoundType(array.getType(), 1);
    }

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
