package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class StringValue implements RightValue {
    private final char[] chars;
}
