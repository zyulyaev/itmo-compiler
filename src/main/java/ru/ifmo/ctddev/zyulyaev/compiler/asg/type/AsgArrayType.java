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
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isAssignableFrom(AsgType type) {
        return type == AsgPredefinedType.NONE || equals(type);
    }

    @Override
    public String toString() {
        return "[" + compound + "]";
    }
}
