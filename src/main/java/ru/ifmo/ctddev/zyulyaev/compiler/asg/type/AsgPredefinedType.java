package ru.ifmo.ctddev.zyulyaev.compiler.asg.type;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
public enum AsgPredefinedType implements AsgType {
    INT("Int", true),
    ANY("Any", false),
    STRING("String", false),
    NONE("None", false);

    private final String name;
    private final boolean primitive;

    AsgPredefinedType(String name, boolean primitive) {
        this.name = name;
        this.primitive = primitive;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isPrimitive() {
        return primitive;
    }

    @Override
    public boolean isAssignableFrom(AsgType type) {
        return type == this || (this == ANY || type == NONE) && !this.isPrimitive();
    }

    @Override
    public String toString() {
        return name;
    }
}
