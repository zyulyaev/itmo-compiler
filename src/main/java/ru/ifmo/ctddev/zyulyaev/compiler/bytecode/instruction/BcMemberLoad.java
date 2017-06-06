package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcRegister;

/**
 * @author zyulyaev
 * @since 05.06.2017
 */
@Data
public class BcMemberLoad implements BcInstruction {
    private final BcRegister object;
    private final AsgDataType.Field field;

    @Override
    public AsgType getResultType() {
        return field.getType();
    }

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
