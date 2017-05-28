package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import lombok.AllArgsConstructor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;

import java.util.Map;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@AllArgsConstructor
public class BcFunctionContext {
    private final Map<AsgVariable, BcVariable> variables;
    private final Map<AsgFunction, BcFunction> functions;

    public BcVariable getVariable(AsgVariable variable) {
        return variables.get(variable);
    }

    public BcFunction getFunction(AsgFunction function) {
        return functions.get(function);
    }
}
