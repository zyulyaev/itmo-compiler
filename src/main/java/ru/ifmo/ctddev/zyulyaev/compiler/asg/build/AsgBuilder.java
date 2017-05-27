package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementList;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.ExternalFunction;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class AsgBuilder {
    public static AsgProgram build(GrammarParser parser, Collection<ExternalFunction> externalFunctions) {
        GrammarParser.ProgramContext program = parser.program();
        Context rootContext = new Context(null);
        BiMap<ExternalFunction, AsgFunction> externalDefinitions = externalFunctions.stream()
            .collect(Collectors.toMap(
                Function.identity(),
                func -> {
                    List<AsgVariable> parameters = IntStream.range(0, func.getParameterCount())
                        .mapToObj(idx -> new AsgVariable("p" + idx))
                        .collect(Collectors.toList());
                    return rootContext.declareFunction(func.getName(), parameters);
                },
                (a, b) -> { throw new IllegalStateException("Duplicate key for: " + a); },
                HashBiMap::create
            ));
        List<AsgFunctionDefinition> definitions = program.definitions().functionDefinition().stream()
            .map(definition -> definition.accept(rootContext.asFunctionDefinitionParser()))
            .collect(Collectors.toList());
        AsgStatementList statements = (AsgStatementList) program.statements()
            .accept(rootContext.asStatementParser());
        return new AsgProgram(definitions, externalDefinitions, statements);
    }
}
