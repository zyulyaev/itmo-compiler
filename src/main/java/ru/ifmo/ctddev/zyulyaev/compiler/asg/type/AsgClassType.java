package ru.ifmo.ctddev.zyulyaev.compiler.asg.type;

import lombok.Getter;
import lombok.Setter;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Getter
@Setter
public class AsgClassType implements AsgType {
    private final String name;
    private Set<AsgClassType> superClasses;
    private List<AsgMethod> methods;

    public AsgClassType(String name) {
        this.name = name;
    }

    public AsgMethod getMethod(String name) {
        AsgMethod ownMethod = methods.stream()
            .filter(method -> method.getName().equals(name))
            .findFirst().orElse(null);
        if (ownMethod != null) {
            return ownMethod;
        } else {
            return superClasses.stream()
                .map(sc -> sc.getMethod(name))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
        }
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isAssignableFrom(AsgType type) {
        return type == AsgPredefinedType.NONE || equals(type) ||
            type instanceof AsgDataType && ((AsgDataType) type).getImplementedClasses().contains(this) ||
            type instanceof AsgClassType && ((AsgClassType) type).getSuperClasses().contains(this);
    }

    @Override
    public String toString() {
        return "class " + name;
    }
}
