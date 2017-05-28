package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class BcVariable {
    private final String name;
}
