package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcNullaryInstructions;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPush;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.ExternalFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcTranslator {
    public BcProgram translate(AsgProgram program) {
        Map<BcFunction, ExternalFunction> externalFunctions = new HashMap<>();
        Map<AsgFunction, BcFunction> functionMap = new HashMap<>();

        for (Map.Entry<ExternalFunction, AsgFunction> entry : program.getExternalDefinitions().entrySet()) {
            AsgFunction asgFunction = entry.getValue();
            BcFunction bcFunction = new BcFunction(
                asgFunction.getName(),
                asgFunction.getParameters().stream()
                    .map(param -> new BcVariable(param.getName()))
                    .collect(Collectors.toList()),
                Collections.emptyList()
            );
            externalFunctions.put(bcFunction, entry.getKey());
            functionMap.put(asgFunction, bcFunction);
        }

        Map<AsgFunction, Map<AsgVariable, BcVariable>> variableMaps = new HashMap<>();
        List<AsgFunctionDefinition> definitions = program.getFunctionDefinitions();

        for (AsgFunctionDefinition definition : definitions) {
            Map<AsgVariable, BcVariable> variables = new HashMap<>();
            List<BcVariable> parameters = new ArrayList<>();
            for (AsgVariable parameter : definition.getFunction().getParameters()) {
                BcVariable bcParameter = new BcVariable(parameter.getName());
                parameters.add(bcParameter);
                variables.put(parameter, bcParameter);
            }
            BcVariableCollector variableCollector = new BcVariableCollector(variables.keySet());
            definition.getBody().accept(variableCollector);
            Map<AsgVariable, BcVariable> definedVariablesMap = variableCollector.getVariables();
            List<BcVariable> definedVariables = new ArrayList<>(definedVariablesMap.values());
            variables.putAll(definedVariablesMap);
            BcFunction bcFunction = new BcFunction(definition.getFunction().getName(), parameters, definedVariables);
            functionMap.put(definition.getFunction(), bcFunction);
            variableMaps.put(definition.getFunction(), variables);
        }

        Map<BcFunction, BcFunctionDefinition> definitionMap = new HashMap<>();

        for (AsgFunctionDefinition definition : definitions) {
            BcFunctionContext context = new BcFunctionContext(variableMaps.get(definition.getFunction()), functionMap);
            BcMemoryOutput output = new BcMemoryOutput();
            BcFunctionTranslator translator = new BcFunctionTranslator(context, output);
            definition.getBody().accept(translator);
            BcFunction function = functionMap.get(definition.getFunction());
            definitionMap.put(function, new BcFunctionDefinition(function, output.getStart()));
        }

        BcVariableCollector variableCollector = new BcVariableCollector(Collections.emptySet());
        program.getStatements().accept(variableCollector);
        List<BcVariable> mainVariables = new ArrayList<>(variableCollector.getVariables().values());
        BcFunction mainFunction = new BcFunction("main", Collections.emptyList(), mainVariables);
        BcFunctionContext mainContext = new BcFunctionContext(variableCollector.getVariables(), functionMap);
        BcMemoryOutput mainOutput = new BcMemoryOutput();
        BcFunctionTranslator mainTranslator = new BcFunctionTranslator(mainContext, mainOutput);
        program.getStatements().accept(mainTranslator);
        mainOutput.write(new BcPush(0));
        mainOutput.write(BcNullaryInstructions.RETURN);

        return new BcProgram(definitionMap, externalFunctions,
            new BcFunctionDefinition(mainFunction, mainOutput.getStart()));
    }
}
