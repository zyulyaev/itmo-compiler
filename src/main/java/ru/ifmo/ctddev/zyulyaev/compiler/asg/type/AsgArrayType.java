package ru.ifmo.ctddev.zyulyaev.compiler.asg.type;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Data
public class AsgArrayType implements AsgType {
    private final AsgType compound;

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAssignableFrom(AsgType type) {
        return type == AsgPredefinedType.NONE
            || type instanceof AsgArrayType && compound.isAssignableFrom(((AsgArrayType) type).compound);
    }
}
