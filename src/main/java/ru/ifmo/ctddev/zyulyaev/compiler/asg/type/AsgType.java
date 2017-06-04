package ru.ifmo.ctddev.zyulyaev.compiler.asg.type;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
public interface AsgType {
    boolean isPrimitive();

    boolean isAssignableFrom(AsgType type);
}
