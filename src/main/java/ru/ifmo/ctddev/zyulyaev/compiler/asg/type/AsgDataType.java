package ru.ifmo.ctddev.zyulyaev.compiler.asg.type;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Getter
@Setter
public class AsgDataType implements AsgType {
    private final List<AsgClassType> implementedClasses = new ArrayList<>();
    private final String name;
    private List<Field> fields;

    public AsgDataType(String name) {
        this.name = name;
    }

    public Field getField(String name) {
        return fields.stream()
            .filter(field -> field.getName().equals(name))
            .findFirst().orElse(null);
    }

    public AsgMethod getMethod(String name) {
        return implementedClasses.stream()
            .map(classType -> classType.getMethod(name))
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    @Override
    public boolean isData() {
        return true;
    }

    @Override
    public boolean isAssignableFrom(AsgType type) {
        return type == AsgPredefinedType.NONE || equals(type);
    }

    @Override
    public String toString() {
        return "data " + name;
    }

    @Data
    public static class Field {
        private final AsgDataType parent;
        private final String name;
        private final AsgType type;
    }
}
