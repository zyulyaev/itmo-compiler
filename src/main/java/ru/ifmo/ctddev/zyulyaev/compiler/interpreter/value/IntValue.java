package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class IntValue implements RightValue {
    private final int value;
}
