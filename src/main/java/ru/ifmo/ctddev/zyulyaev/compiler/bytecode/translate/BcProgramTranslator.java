package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcNullaryInstructions;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPush;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcProgramTranslator {
    private final Map<BcFunction, AsgExternalFunction> externalFunctions = new HashMap<>();
    private final Map<AsgFunction, BcFunction> functionMap = new HashMap<>();
    private final AsgProgram program;

    private BcProgramTranslator(AsgProgram program) {
        this.program = program;
        for (Map.Entry<AsgFunction, AsgExternalFunction> entry : program.getExternalFunctions().entrySet()) {
            AsgFunction asgFunction = entry.getKey();
            BcFunction bcFunction = new BcFunction(
                asgFunction.getName(),
                asgFunction.getParameters().stream()
                    .map(param -> new BcVariable(param.getName()))
                    .collect(Collectors.toList())
            );
            externalFunctions.put(bcFunction, entry.getValue());
            functionMap.put(asgFunction, bcFunction);
        }
        for (AsgFunctionDefinition definition : program.getFunctionDefinitions()) {
            List<BcVariable> parameters = definition.getFunction().getParameters().stream()
                .map(var -> new BcVariable(var.getName()))
                .collect(Collectors.toList());
            BcFunction function = new BcFunction(definition.getFunction().getName(), parameters);
            functionMap.put(definition.getFunction(), function);
        }
    }

    public static BcProgram translate(AsgProgram program) {
        return new BcProgramTranslator(program).translate();
    }

    private BcProgram translate() {
        Map<BcFunction, BcFunctionDefinition> definitionMap = new HashMap<>();
        for (AsgFunctionDefinition definition : program.getFunctionDefinitions()) {
            BcFunctionDefinition translated = translate(definition);
            definitionMap.put(translated.getFunction(), translated);
        }
        BcFunctionDefinition main =
            translate(new BcFunction("main", Collections.emptyList()), Collections.emptyMap(), program.getMain());
        return new BcProgram(definitionMap, externalFunctions, main);
    }

    private BcFunctionDefinition translate(AsgFunctionDefinition definition) {
        AsgFunction asgFunction = definition.getFunction();
        BcFunction bcFunction = functionMap.get(asgFunction);
        Map<AsgVariable, BcVariable> parameters = IntStream.range(0, asgFunction.getParameters().size()).boxed()
            .collect(Collectors.toMap(
                i -> asgFunction.getParameters().get(i),
                i -> bcFunction.getParameters().get(i)
            ));
         return translate(bcFunction, parameters, definition.getBody());
    }

    private BcFunctionDefinition translate(BcFunction function, Map<AsgVariable, BcVariable> parameter,
        AsgStatement body)
    {
        BcLabel returnLabel = new BcLabel(function.getName() + "_cleanup");
        BcFunctionContext functionContext = new BcFunctionContext(functionMap, parameter, returnLabel);
        BcContext functionRootContext = new BcContext(functionContext);
        BcOutput output = new BcOutput();
        try (BcTranslator translator = new BcTranslator(functionRootContext, output)) {
            body.accept(translator);
            // in case return not called, return 0
            output.write(new BcPush(0));
            output.write(returnLabel);
        }
        output.write(BcNullaryInstructions.RETURN);
        return new BcFunctionDefinition(function, functionContext.getLocalVariables(), output.getLines());
    }
}
