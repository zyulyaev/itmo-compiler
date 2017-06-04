package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
@Data
@AllArgsConstructor
public class VarValue implements LeftValue {
    private RightValue value;

    @Override
    public void set(RightValue value) {
        this.value = value;
    }

    @Override
    public RightValue asRightValue() {
        return value;
    }
}
