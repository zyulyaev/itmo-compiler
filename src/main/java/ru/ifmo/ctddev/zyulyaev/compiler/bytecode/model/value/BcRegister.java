package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 05.06.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class BcRegister implements BcValue {
    private final int index;
    private final AsgType type;

    @Override
    public AsgType getType() {
        return type;
    }

    @Override
    public <T> T accept(BcValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
