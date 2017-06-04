package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.DataTypeValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.LeftValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.RightValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.VarValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class InterpreterContext {
    private final Map<AsgVariable, LeftValue> valuesMap = new HashMap<>();
    private final FunctionTable table;
    private final InterpreterContext parent;

    InterpreterContext(FunctionTable table, InterpreterContext parent) {
        this.table = table;
        this.parent = parent;
    }

    private boolean hasVariable(AsgVariable variable) {
        return valuesMap.containsKey(variable) || parent != null && parent.hasVariable(variable);
    }

    LeftValue getVariableValue(AsgVariable variable) {
        if (valuesMap.containsKey(variable)) {
            return valuesMap.get(variable);
        } else {
            return parent == null ? null : parent.getVariableValue(variable);
        }
    }

    LeftValue getOrDefineValue(AsgVariable variable) {
        if (hasVariable(variable)) {
            return getVariableValue(variable);
        } else {
            return defineVariable(variable);
        }
    }

    LeftValue defineVariable(AsgVariable variable) {
        VarValue value = new VarValue(null);
        valuesMap.put(variable, value);
        return value;
    }

    ExpressionInterpreter asExpressionInterpreter() {
        return new ExpressionInterpreter(this);
    }

    StatementInterpreter asStatementInterpreter() {
        return new StatementInterpreter(this);
    }

    InterpreterContext createChild() {
        return new InterpreterContext(table, this);
    }

    RightValue callFunction(AsgFunction function, List<RightValue> arguments) {
        return table.callFunction(function, arguments);
    }

    RightValue callMethod(DataTypeValue object, AsgMethod method, List<RightValue> arguments) {
        return table.callMethod(object, method, arguments);
    }
}
