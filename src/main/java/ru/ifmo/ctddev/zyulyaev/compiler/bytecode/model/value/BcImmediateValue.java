package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 05.06.2017
 */
@Data
public class BcImmediateValue implements BcValue {
    private final int value;

    @Override
    public AsgType getType() {
        return AsgPredefinedType.INT;
    }

    @Override
    public <T> T accept(BcValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
