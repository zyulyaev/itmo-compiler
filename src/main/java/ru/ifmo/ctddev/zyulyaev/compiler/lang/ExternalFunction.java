package ru.ifmo.ctddev.zyulyaev.compiler.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class ExternalFunction {
    private final String name;
    private final int parameterCount;
}
