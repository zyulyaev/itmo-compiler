package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class Context {
    private final Map<String, AsgFunction> functionMap = new HashMap<>();
    private final Map<String, AsgVariable> variablesMap = new HashMap<>();
    private final Context parent;

    Context(Context parent) {
        this.parent = parent;
    }

    AsgFunction resolveFunction(String name) {
        if (functionMap.containsKey(name)) {
            return functionMap.get(name);
        } else {
            return parent == null ? null : parent.resolveFunction(name);
        }
    }

    AsgFunction declareFunction(String name, List<AsgVariable> parameters) {
        // todo function must create variables on each call
        if (resolveFunction(name) != null) {
            throw new IllegalStateException("Function " + name + " already declared");
        }
        AsgFunction function = new AsgFunction(name, parameters);
        functionMap.put(name, function);
        return function;
    }

    AsgVariable resolveVariable(String name) {
        if (variablesMap.containsKey(name)) {
            return variablesMap.get(name);
        } else {
            return parent == null ? null : parent.resolveVariable(name);
        }
    }

    AsgVariable resolveOrDeclareVariable(String name) {
        AsgVariable variable = resolveVariable(name);
        if (variable == null) {
            variable = new AsgVariable(name);
            variablesMap.put(name, variable);
        }
        return variable;
    }

    AsgVariable declareVariable(String name) {
        AsgVariable variable = new AsgVariable(name);
        variablesMap.put(name, variable);
        return variable;
    }

    FunctionDefinitionParser asFunctionDefinitionParser() {
        return new FunctionDefinitionParser(this);
    }

    StatementParser asStatementParser() {
        return new StatementParser(this);
    }

    ExpressionParser asExpressionParser() {
        return new ExpressionParser(this);
    }

    Context createChild() {
        return new Context(this);
    }
}
