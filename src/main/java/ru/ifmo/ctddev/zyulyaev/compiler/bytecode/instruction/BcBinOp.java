package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgBinaryOperator;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcBinOp implements BcInstruction {
    private final AsgBinaryOperator operator;

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
