package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;

import java.util.List;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgFunctionDefinition {
    private final AsgFunction function;
    private final List<AsgVariable> parameters;
    private final AsgStatement body;
}
