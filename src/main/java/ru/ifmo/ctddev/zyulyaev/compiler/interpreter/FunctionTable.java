package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.ExternalFunction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class FunctionTable {
    private final Map<AsgFunction, AsgFunctionDefinition> definitionMap;
    private final Map<AsgFunction, ExternalFunction> externalFunctionMap;
    private final Map<ExternalFunction, Function<List<Value>, Value>> externalFunctionDefinitionMap;

    public FunctionTable(Collection<AsgFunctionDefinition> definitions,
        Map<AsgFunction, ExternalFunction> externalFunctionMap,
        Map<ExternalFunction, Function<List<Value>, Value>> externalFunctionDefinitionMap)
    {
        this.definitionMap = definitions.stream()
            .collect(Collectors.toMap(AsgFunctionDefinition::getFunction, Function.identity()));
        this.externalFunctionMap = externalFunctionMap;
        this.externalFunctionDefinitionMap = externalFunctionDefinitionMap;
    }

    public Value callFunction(AsgFunction function, List<Value> arguments) {
        if (definitionMap.containsKey(function)) {
            InterpreterContext callContext = new InterpreterContext(this, null);
            for (int i = 0; i < arguments.size(); i++) {
                AsgVariable parameter = function.getParameters().get(i);
                callContext.assignVariable(parameter, arguments.get(i));
            }
            AsgFunctionDefinition definition = definitionMap.get(function);
            return definition.getBody().accept(callContext.asStatementInterpreter());
        } else if (externalFunctionMap.containsKey(function)) {
            Function<List<Value>, Value> functionDefinition =
                externalFunctionDefinitionMap.get(externalFunctionMap.get(function));
            return functionDefinition.apply(arguments);
        } else {
            throw new IllegalArgumentException("Function not found: " + function);
        }
    }
}
