package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class Context {
    private final Map<String, AsgVariable> variablesMap = new HashMap<>();
    private final Environment environment;
    private final Context parent;

    Context(Environment environment, Context parent) {
        this.environment = environment;
        this.parent = parent;
    }

    AsgFunction resolveFunction(String name, List<AsgType> argumentTypes) {
        return environment.getFunction(name, argumentTypes);
    }

    AsgVariable resolveVariable(String name) {
        if (variablesMap.containsKey(name)) {
            return variablesMap.get(name);
        } else {
            return parent == null ? null : parent.resolveVariable(name);
        }
    }

    AsgVariable resolveOrDeclareVariable(String name, AsgType type) {
        AsgVariable variable = resolveVariable(name);
        if (variable == null) {
            variable = new AsgVariable(name, type);
            variablesMap.put(name, variable);
        } else if (!variable.getType().equals(type)) {
            throw new IllegalArgumentException("Types don't match: " + name);
        }
        return variable;
    }

    void declareVariable(AsgVariable variable) {
        if (variablesMap.containsKey(variable.getName())) {
            throw new IllegalStateException("Variable already defined: " + variable);
        }
        variablesMap.put(variable.getName(), variable);
    }


    StatementParser asStatementParser() {
        return new StatementParser(this);
    }

    ExpressionParser asExpressionParser() {
        return new ExpressionParser(this);
    }

    TypeParser asTypeParser() {
        return environment.asTypeParser();
    }

    Context createChild() {
        return new Context(environment, this);
    }
}
