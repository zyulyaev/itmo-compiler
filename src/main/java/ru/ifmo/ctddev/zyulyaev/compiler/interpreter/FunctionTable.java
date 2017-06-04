package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgImplDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethodDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.DataTypeValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.NoneValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.RightValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class FunctionTable {
    private final Map<AsgFunction, AsgFunctionDefinition> definitionMap;
    private final Table<AsgDataType, AsgClassType, AsgImplDefinition> implDefinitionTable;
    private final Map<AsgFunction, AsgExternalFunction> externalFunctionMap;
    private final Map<AsgExternalFunction, Function<List<RightValue>, RightValue>> externalFunctionDefinitionMap;

    FunctionTable(Collection<AsgFunctionDefinition> definitions, Collection<AsgImplDefinition> implDefinitions,
        Map<AsgFunction, AsgExternalFunction> externalFunctionMap,
        Map<AsgExternalFunction, Function<List<RightValue>, RightValue>> externalFunctionDefinitionMap)
    {
        this.definitionMap = definitions.stream()
            .collect(Collectors.toMap(AsgFunctionDefinition::getFunction, Function.identity()));
        this.implDefinitionTable = implDefinitions.stream()
            .collect(Collector.of(
                HashBasedTable::create,
                (table, def) -> table.put(def.getDataType(), def.getClassType(), def),
                (left, right) -> {
                    left.putAll(right);
                    return left;
                }
            ));
        this.externalFunctionMap = externalFunctionMap;
        this.externalFunctionDefinitionMap = externalFunctionDefinitionMap;
    }

    RightValue callFunction(AsgFunction function, List<RightValue> arguments) {
        if (definitionMap.containsKey(function)) {
            AsgFunctionDefinition definition = definitionMap.get(function);
            InterpreterContext callContext = new InterpreterContext(this, null);
            for (int i = 0; i < arguments.size(); i++) {
                AsgVariable parameter = definition.getParameters().get(i);
                callContext.defineVariable(parameter).set(arguments.get(i));
            }
            Value result = definition.getBody().accept(callContext.asStatementInterpreter());
            return result == null ? NoneValue.INSTANCE : result.asRightValue();
        } else if (externalFunctionMap.containsKey(function)) {
            Function<List<RightValue>, RightValue> functionDefinition =
                externalFunctionDefinitionMap.get(externalFunctionMap.get(function));
            return functionDefinition.apply(arguments);
        } else {
            throw new IllegalArgumentException("Function not found: " + function);
        }
    }

    RightValue callMethod(DataTypeValue object, AsgMethod method, List<RightValue> arguments) {
        AsgImplDefinition implementation = implDefinitionTable.get(object.getType(), method.getParent());
        AsgMethodDefinition definition = implementation.getDefinition(method);
        InterpreterContext callContext = new InterpreterContext(this, null);
        callContext.defineVariable(definition.getThisValue()).set(object);
        for (int i = 0; i < arguments.size(); i++) {
            AsgVariable parameter = definition.getParameters().get(i);
            callContext.defineVariable(parameter).set(arguments.get(i));
        }
        Value result = definition.getBody().accept(callContext.asStatementInterpreter());
        return result == null ? NoneValue.INSTANCE : result.asRightValue();
    }
}
