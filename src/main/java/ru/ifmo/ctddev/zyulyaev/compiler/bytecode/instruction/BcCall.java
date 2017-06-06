package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;

import java.util.List;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcCall implements BcInstruction {
    private final AsgFunction function;
    private final List<BcValue> arguments;

    @Override
    public AsgType getResultType() {
        return function.getReturnType();
    }

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
