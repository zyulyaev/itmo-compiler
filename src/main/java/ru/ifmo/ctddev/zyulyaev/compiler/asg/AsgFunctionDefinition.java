package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgFunctionDefinition {
    private final AsgFunction function;
    private final AsgStatement body;
}
