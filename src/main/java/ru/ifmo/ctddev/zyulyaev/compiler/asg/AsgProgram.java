package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;

import java.util.List;
import java.util.Map;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgProgram {
    private final List<AsgFunctionDefinition> functionDefinitions;
    private final List<AsgDataType> dataDefinitions;
    private final List<AsgClassDefinition> classDefinitions;
    private final List<AsgImplDefinition> implDefinitions;

    private final Map<AsgFunction, AsgExternalFunction> externalFunctions;

    private final AsgStatement main;
}
