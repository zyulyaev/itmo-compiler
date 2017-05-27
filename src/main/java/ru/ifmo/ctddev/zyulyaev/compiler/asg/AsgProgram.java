package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import com.google.common.collect.BiMap;
import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementList;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.ExternalFunction;

import java.util.List;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgProgram {
    private final List<AsgFunctionDefinition> functionDefinitions;
    private final BiMap<ExternalFunction, AsgFunction> externalDefinitions;
    private final AsgStatementList statements;
}
