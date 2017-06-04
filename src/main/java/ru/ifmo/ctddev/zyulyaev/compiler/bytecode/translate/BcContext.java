package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 30.05.2017
 */
public class BcContext {
    private final Map<AsgVariable, BcVariable> variableMap = new LinkedHashMap<>();
    private final List<BcVariable> generated = new ArrayList<>();
    private final BcFunctionContext functionContext;
    private final BcContext parent;

    public BcContext(BcFunctionContext parent) {
        this.functionContext = parent;
        this.parent = null;
    }

    public BcContext(BcContext parent) {
        this.functionContext = parent.functionContext;
        this.parent = parent;
    }

    private boolean hasVariable(AsgVariable variable) {
        return functionContext.hasParameter(variable) ||
            variableMap.containsKey(variable) ||
            parent != null && parent.hasVariable(variable);
    }

    public BcFunction getFunction(AsgFunction function) {
        return functionContext.getFunction(function);
    }

    public BcVariable getVariable(AsgVariable variable) {
        if (functionContext.hasParameter(variable)) {
            return functionContext.getParameter(variable);
        } if (variableMap.containsKey(variable)) {
            return variableMap.get(variable);
        } else if (parent != null && parent.hasVariable(variable)) {
            return parent.getVariable(variable);
        } else {
            BcVariable var = functionContext.reserveVariable();
            variableMap.put(variable, var);
            return var;
        }
    }

    public BcVariable reserveVariable() {
        BcVariable variable = functionContext.reserveVariable();
        generated.add(variable);
        return variable;
    }

    public BcLabel reserveLabel(String name) {
        return functionContext.reserveLabel(name);
    }

    public Stream<BcVariable> cleanup() {
        functionContext.freeVariables(variableMap.values());
        functionContext.freeVariables(generated);
        return Stream.concat(
            variableMap.values().stream(),
            generated.stream()
        );
    }

    public BcLabel getReturnLabel() {
        return functionContext.getReturnLabel();
    }
}
