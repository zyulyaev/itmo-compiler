package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.BinaryOperator;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcBinOp implements BcInstruction {
    private final BinaryOperator operator;

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
