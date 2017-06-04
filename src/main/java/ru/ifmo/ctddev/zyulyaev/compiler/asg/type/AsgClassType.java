package ru.ifmo.ctddev.zyulyaev.compiler.asg.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgClassDefinition;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class AsgClassType implements AsgType {
    private final AsgClassDefinition classDefinition;

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAssignableFrom(AsgType type) {
        // todo
        throw new UnsupportedOperationException();
    }
}
