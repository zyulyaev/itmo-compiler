package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;

import java.util.List;

/**
 * @author zyulyaev
 * @since 05.06.2017
 */
@Data
public class BcMethodCall implements BcInstruction {
    private final BcRegister object;
    private final AsgMethod method;
    private final List<BcValue> arguments;

    @Override
    public AsgType getResultType() {
        return method.getReturnType();
    }

    @Override
    public <T> T accept(BcInstructionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
