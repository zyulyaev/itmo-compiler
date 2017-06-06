package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 05.06.2017
 */
public class BcNoneValue implements BcValue {
    public static final BcNoneValue INSTANCE = new BcNoneValue();

    private BcNoneValue() {
    }

    @Override
    public AsgType getType() {
        return AsgPredefinedType.NONE;
    }

    @Override
    public <T> T accept(BcValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
