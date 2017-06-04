package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.NoneValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.RightValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class FunctionTable {
    private final Map<AsgFunction, AsgFunctionDefinition> definitionMap;
    private final Map<AsgFunction, AsgExternalFunction> externalFunctionMap;
    private final Map<AsgExternalFunction, Function<List<RightValue>, RightValue>> externalFunctionDefinitionMap;

    FunctionTable(Collection<AsgFunctionDefinition> definitions,
        Map<AsgFunction, AsgExternalFunction> externalFunctionMap,
        Map<AsgExternalFunction, Function<List<RightValue>, RightValue>> externalFunctionDefinitionMap)
    {
        this.definitionMap = definitions.stream()
            .collect(Collectors.toMap(AsgFunctionDefinition::getFunction, Function.identity()));
        this.externalFunctionMap = externalFunctionMap;
        this.externalFunctionDefinitionMap = externalFunctionDefinitionMap;
    }

    RightValue callFunction(AsgFunction function, List<RightValue> arguments) {
        if (definitionMap.containsKey(function)) {
            InterpreterContext callContext = new InterpreterContext(this, null);
            for (int i = 0; i < arguments.size(); i++) {
                AsgVariable parameter = function.getParameters().get(i);
                callContext.defineVariable(parameter).set(arguments.get(i));
            }
            AsgFunctionDefinition definition = definitionMap.get(function);
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
}
