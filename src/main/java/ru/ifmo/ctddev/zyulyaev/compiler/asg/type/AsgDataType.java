package ru.ifmo.ctddev.zyulyaev.compiler.asg.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class AsgDataType implements AsgType {
    private final String name;
    private final List<Field> fields;

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAssignableFrom(AsgType type) {
        return type == AsgPredefinedType.NONE || equals(type);
    }

    @Data
    public static class Field {
        private final String name;
        private final AsgType type;
    }
}
