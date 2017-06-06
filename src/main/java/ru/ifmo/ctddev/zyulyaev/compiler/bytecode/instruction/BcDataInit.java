package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;

import java.util.Map;

/**
 * @author zyulyaev
 * @since 05.06.2017
 */
@Data
public class BcDataInit implements BcInstruction {
    private final AsgDataType type;
    private final Map<AsgDataType.Field, BcValue> values;

    @Override
    public AsgType getResultType() {
        return type;
    }

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
