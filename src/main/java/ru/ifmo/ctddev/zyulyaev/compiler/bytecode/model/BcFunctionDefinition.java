package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class BcFunctionDefinition {
    private final BcFunction function;
    private final List<BcVariable> localVariables;
    private final List<BcLine> body;
}
