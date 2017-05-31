package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcArrayPointer implements BcPointer {
    private final BcValue[] values;
    private final int index;

    @Override
    public void set(BcValue value) {
        values[index] = value;
    }

    @Override
    public BcValue get() {
        return values[index];
    }

    @Override
    public BcPointer shift(int offset) {
        return new BcArrayPointer(values, index + offset);
    }

    @Override
    public int length() {
        return values.length - index;
    }
}
