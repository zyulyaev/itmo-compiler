package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLine;

import java.util.List;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class BcFunctionDefinition {
    private final AsgFunction function;
    private final List<AsgVariable> parameters;
    private final List<AsgVariable> localVariables;
    private final List<BcLine> body;
}
