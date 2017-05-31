package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import lombok.Getter;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcFunctionContext {
    @Getter
    private final List<BcVariable> localVariables = new ArrayList<>();
    private final Queue<BcVariable> pool = new ArrayDeque<>();
    private final Map<String, Integer> labelCounters = new HashMap<>();

    private final Map<AsgFunction, BcFunction> functions;
    private final Map<AsgVariable, BcVariable> parameters;
    @Getter
    private final BcLabel returnLabel;

    public BcFunctionContext(Map<AsgFunction, BcFunction> functions, Map<AsgVariable, BcVariable> parameters,
        BcLabel returnLabel)
    {
        this.functions = functions;
        this.parameters = parameters;
        this.returnLabel = returnLabel;
    }

    public BcFunction getFunction(AsgFunction function) {
        return functions.get(function);
    }

    public BcVariable getParameter(AsgVariable variable) {
        return parameters.get(variable);
    }

    public boolean hasParameter(AsgVariable variable) {
        return parameters.containsKey(variable);
    }

    public BcVariable reserveVariable() {
        if (pool.isEmpty()) {
            BcVariable var = new BcVariable("v_" + localVariables.size());
            localVariables.add(var);
            pool.add(var);
        }
        return pool.poll();
    }

    public BcLabel reserveLabel(String name) {
        int counter = labelCounters.merge(name, 1, Integer::sum);
        return new BcLabel("l_" + name + "_" + counter);
    }

    public void freeVariables(Collection<BcVariable> variable) {
        assert localVariables.containsAll(variable);
        pool.addAll(variable);
    }
}
