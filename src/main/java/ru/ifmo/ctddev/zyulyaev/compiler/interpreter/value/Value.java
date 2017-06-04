package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public interface Value {
    LeftValue asLeftValue();

    RightValue asRightValue();
}