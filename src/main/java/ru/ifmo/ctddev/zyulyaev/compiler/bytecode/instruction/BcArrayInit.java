package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgArrayType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;

import java.util.List;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcArrayInit implements BcInstruction {
    private final List<BcValue> values;
    private final AsgArrayType arrayType;

    @Override
    public AsgType getResultType() {
        return arrayType;
    }

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
