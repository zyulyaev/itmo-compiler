package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

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

    AsgMethod resolveMethod(AsgType type, String name) {
        if (type instanceof AsgClassType) {
            return ((AsgClassType) type).getMethod(name);
        } else if (type instanceof AsgDataType) {
            return ((AsgDataType) type).getMethod(name);
        } else {
            return null;
        }
    }

    AsgVariable resolveVariable(String name) {
        if (variablesMap.containsKey(name)) {
            return variablesMap.get(name);
        } else {
            return parent == null ? null : parent.resolveVariable(name);
        }
    }

    AsgVariable resolveOrDeclareVariable(String name, AsgType type, boolean readOnly) {
        AsgVariable variable = resolveVariable(name);
        if (variable == null) {
            variable = declareVariable(name, type, readOnly);
        } else if (!variable.getType().equals(type) || readOnly != variable.isReadOnly()) {
            throw new IllegalArgumentException("Types don't match: " + name);
        }
        return variable;
    }

    AsgVariable declareVariable(String name, AsgType type, boolean readOnly) {
        if (variablesMap.containsKey(name)) {
            throw new IllegalStateException("Variable already defined: " + name);
        }
        AsgVariable variable = new AsgVariable(name, type, readOnly);
        variablesMap.put(name, variable);
        return variable;
    }

    AsgType resolveType(String name) {
        return environment.getType(name);
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
