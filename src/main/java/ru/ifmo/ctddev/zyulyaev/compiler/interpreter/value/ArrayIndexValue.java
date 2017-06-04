package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
@Data
public class ArrayIndexValue implements LeftValue {
    private final ArrayValue array;
    private final int index;

    @Override
    public RightValue asRightValue() {
        return array.get(index);
    }

    @Override
    public void set(RightValue value) {
        array.set(index, value);
    }
}
