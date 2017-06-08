package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethodDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcReturn;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcMethodDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcNoneValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcProgramTranslator {
    private final AsgProgram program;

    private BcProgramTranslator(AsgProgram program) {
        this.program = program;
    }

    public static BcProgram translate(AsgProgram program) {
        return new BcProgramTranslator(program).translate();
    }

    private BcProgram translate() {
        List<BcFunctionDefinition> functions = program.getFunctionDefinitions().stream()
            .map(this::translate)
            .collect(Collectors.toList());
        List<BcMethodDefinition> methods = program.getImplDefinitions().stream()
            .flatMap(impl -> impl.getDefinitions().stream()
                .map(def -> translate(impl.getDataType(), def)))
            .collect(Collectors.toList());
        AsgFunction mainFunction = new AsgFunction("main", Collections.emptyList(), AsgPredefinedType.NONE);
        BcFunctionDefinition main =
            translate(new AsgFunctionDefinition(mainFunction, Collections.emptyList(), program.getMain()));
        return new BcProgram(
            program.getDataDefinitions(),
            program.getClassDefinitions(),
            functions,
            methods,
            program.getExternalFunctions(),
            main
        );
    }

    private BcFunctionDefinition translate(AsgFunctionDefinition definition) {
        AsgFunction function = definition.getFunction();
        List<AsgVariable> parameters = definition.getParameters();
        BcBuilder builder = new BcBuilder(new HashSet<>(parameters));
        definition.getBody().accept(new BcTranslator(builder));
        // in case return not called, return none
        builder.write(new BcReturn(BcNoneValue.INSTANCE));
        List<AsgVariable> localVariables = new ArrayList<>(builder.getLocalVariables());
        return new BcFunctionDefinition(function, parameters, localVariables, builder.getLines());
    }

    private BcMethodDefinition translate(AsgDataType dataType, AsgMethodDefinition definition) {
        AsgMethod method = definition.getMethod();
        List<AsgVariable> parameters = definition.getParameters();
        Set<AsgVariable> predefined = new HashSet<>(parameters);
        predefined.add(definition.getThisValue());
        BcBuilder builder = new BcBuilder(predefined);
        definition.getBody().accept(new BcTranslator(builder));
        // in case return not called, return none
        builder.write(new BcReturn(BcNoneValue.INSTANCE));
        List<AsgVariable> localVariables = new ArrayList<>(builder.getLocalVariables());
        return new BcMethodDefinition(dataType, method, definition.getThisValue(), parameters, localVariables, builder.getLines());
    }
}
