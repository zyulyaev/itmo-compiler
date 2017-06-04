package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
public interface LeftValue extends Value {
    @Override
    default LeftValue asLeftValue() {
        return this;
    }

    void set(RightValue value);
}
