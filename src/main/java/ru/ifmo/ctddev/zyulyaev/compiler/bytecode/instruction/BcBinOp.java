package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgBinaryOperator;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcBinOp implements BcInstruction {
    private final AsgBinaryOperator operator;
    private final BcValue left;
    private final BcValue right;

    @Override
    public AsgType getResultType() {
        return AsgPredefinedType.INT;
    }

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
