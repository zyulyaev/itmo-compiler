package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;

import java.util.Map;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcProgram {
    private final Map<BcFunction, BcFunctionDefinition> functions;
    private final Map<BcFunction, AsgExternalFunction> externalFunctions;
    private final BcFunctionDefinition main;
}
