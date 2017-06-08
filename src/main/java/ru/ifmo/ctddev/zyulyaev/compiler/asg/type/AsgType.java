package ru.ifmo.ctddev.zyulyaev.compiler.asg.type;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
public interface AsgType {
    default boolean isPrimitive() {
        return false;
    }

    default boolean isData() {
        return false;
    }

    default boolean isClass() {
        return false;
    }

    default boolean isArray() {
        return false;
    }

    boolean isAssignableFrom(AsgType type);
}
