package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.ArrayValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
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

    private Value getVariableValue(AsgVariable variable) {
        if (valuesMap.containsKey(variable)) {
            return valuesMap.get(variable);
        } else {
            return parent == null ? null : parent.getVariableValue(variable);
        }
    }

    private Value locateValue(LeftValue leftValue, int skipDepth) {
        Value value = getVariableValue(leftValue.getVariable());
        List<IntValue> indexes = leftValue.getIndexes();
        for (int i = 0; i < indexes.size() - skipDepth; i++) {
            value = value.asArray().get(indexes.get(i).getValue());
        }
        return value;
    }

    public void assignValue(LeftValue leftValue, Value value) {
        if (leftValue.isPlainVariable()) {
            AsgVariable variable = leftValue.getVariable();
            if (parent == null || !parent.hasVariable(variable)) {
                valuesMap.put(variable, value);
            } else {
                parent.assignValue(leftValue, value);
            }
        } else {
            ArrayValue array = (ArrayValue) locateValue(leftValue, 1);
            List<IntValue> indexes = leftValue.getIndexes();
            array.set(indexes.get(indexes.size() - 1).getValue(), value);
        }
    }

    public Value getVariableValue(LeftValue leftValue) {
        return locateValue(leftValue, 0);
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
