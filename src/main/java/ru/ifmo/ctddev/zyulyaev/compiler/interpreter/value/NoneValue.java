package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
public class NoneValue implements RightValue {
    public static final NoneValue INSTANCE = new NoneValue();

    private NoneValue() {
    }
}
