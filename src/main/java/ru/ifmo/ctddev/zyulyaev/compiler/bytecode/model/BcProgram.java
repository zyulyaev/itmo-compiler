package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;

import java.util.List;
import java.util.Map;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class BcProgram {
    private final List<AsgDataType> dataDefinitions;
    private final List<AsgClassType> classDefinitions;
    private final List<BcFunctionDefinition> functions;
    private final List<BcMethodDefinition> methods;

    private final Map<AsgFunction, AsgExternalFunction> externalFunctions;

    private final BcFunctionDefinition main;
}
