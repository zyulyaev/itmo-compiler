package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class InterpreterContext {
    private final Map<AsgVariable, Value> valuesMap = new HashMap<>();
    private final FunctionTable table;
    private final InterpreterContext parent;

    public InterpreterContext(FunctionTable table, InterpreterContext parent) {
        this.table = table;
        this.parent = parent;
    }

    private boolean hasVariable(AsgVariable variable) {
        return valuesMap.containsKey(variable) || parent != null && parent.hasVariable(variable);
    }

    public void assignVariable(AsgVariable variable, Value value) {
        if (parent == null || !parent.hasVariable(variable)) {
            valuesMap.put(variable, value);
        } else {
            parent.assignVariable(variable, value);
        }
    }

    public Value getVariableValue(AsgVariable variable) {
        if (valuesMap.containsKey(variable)) {
            return valuesMap.get(variable);
        } else {
            return parent.getVariableValue(variable);
        }
    }

    public ExpressionInterpreter asExpressionInterpreter() {
        return new ExpressionInterpreter(this);
    }

    public StatementInterpreter asStatementInterpreter() {
        return new StatementInterpreter(this);
    }

    public InterpreterContext createChild() {
        return new InterpreterContext(table, this);
    }

    public Value callFunction(AsgFunction function, List<Value> arguments) {
        return table.callFunction(function, arguments);
    }
}
