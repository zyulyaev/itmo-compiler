package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.GrammarBaseVisitor;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgArrayType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class AsgBuilder {
    private final GrammarParser parser;
    private final Environment environment;

    private AsgBuilder(GrammarParser parser, Collection<AsgExternalFunction> externalFunctions) {
        this.parser = parser;
        this.environment = new Environment(externalFunctions);
    }

    public static AsgProgram build(GrammarParser parser, Collection<AsgExternalFunction> externalFunctions) {
        return new AsgBuilder(parser, externalFunctions).build();
    }

    private AsgProgram build() {
        GrammarParser.ProgramContext program = parser.program();

        List<GrammarParser.FunctionDefinitionContext> functionDefinitions = new ArrayList<>();
        List<GrammarParser.DataDefinitionContext> dataDefinitions = new ArrayList<>();
        List<GrammarParser.ClassDefinitionContext> classDefinitions = new ArrayList<>();
        List<GrammarParser.ImplDefinitionContext> implDefinitions = new ArrayList<>();

        program.definitions().accept(new GrammarBaseVisitor<Void>() {
            @Override
            public Void visitFunctionDefinition(GrammarParser.FunctionDefinitionContext ctx) {
                functionDefinitions.add(ctx);
                return null;
            }

            @Override
            public Void visitDataDefinition(GrammarParser.DataDefinitionContext ctx) {
                dataDefinitions.add(ctx);
                return null;
            }

            @Override
            public Void visitClassDefinition(GrammarParser.ClassDefinitionContext ctx) {
                classDefinitions.add(ctx);
                return null;
            }

            @Override
            public Void visitImplDefinition(GrammarParser.ImplDefinitionContext ctx) {
                implDefinitions.add(ctx);
                return null;
            }
        });

        List<AsgDataType> dataTypes = parseDataDefinitions(dataDefinitions);
        functionDefinitions.forEach(this::parseFunctionDeclaration);
        List<AsgFunctionDefinition> definedFunctions =
            functionDefinitions.stream().map(this::parseFunctionDefinition).collect(Collectors.toList());

        Context mainContext = new Context(environment, null);
        AsgStatement mainBody = program.statements().accept(mainContext.asStatementParser());

        return new AsgProgram(
            definedFunctions,
            dataTypes,
            Collections.emptyList(),
            Collections.emptyList(),
            environment.getExternalFunctions(),
            mainBody
        );
    }

    private List<AsgDataType> parseDataDefinitions(List<GrammarParser.DataDefinitionContext> ctxs) {
        List<AsgDataType> result = new ArrayList<>();

        Map<String, GrammarParser.DataDefinitionContext> dataTypeByName =
            ctxs.stream().collect(Collectors.toMap(ctx -> ctx.name.getText(), Function.identity()));

        DataTypeDependencyGraph graph = DataTypeDependencyGraphBuilder.build(ctxs);
        graph.findCycle()
            .ifPresent(cycle -> {
                throw new IllegalStateException("Data cycle detected: " +
                    cycle.stream().collect(Collectors.joining(" -> ")));
            });

        for (String typeName : graph.topologySort()) {
            if (environment.containsType(typeName)) {
                continue;
            }
            GrammarParser.DataDefinitionContext definition = dataTypeByName.get(typeName);
            if (definition == null) {
                throw new IllegalArgumentException("Undefined type: " + typeName);
            }
            AsgDataType type = new AsgDataType(
                definition.name.getText(),
                definition.fields().field().stream()
                    .map(field -> new AsgDataType.Field(field.name.getText(),
                        field.type().accept(new GrammarBaseVisitor<AsgType>() {
                            @Override
                            public AsgType visitPlainType(GrammarParser.PlainTypeContext ctx) {
                                return environment.getType(ctx.id().getText());
                            }

                            @Override
                            public AsgType visitArrayType(GrammarParser.ArrayTypeContext ctx) {
                                return new AsgArrayType(ctx.type().accept(this));
                            }
                        })))
                    .collect(Collectors.toList())
            );
            environment.defineType(type);
            result.add(type);
        }
        return result;
    }

    private void parseFunctionDeclaration(GrammarParser.FunctionDefinitionContext ctx) {
        String name = ctx.name.getText();
        List<AsgVariable> parameters = ctx.parameters().parameter().stream()
            .map(param -> new AsgVariable(param.id().getText(), param.type().accept(environment.asTypeParser())))
            .collect(Collectors.toList());
        AsgType returnType = ctx.returnType.accept(environment.asTypeParser());
        AsgFunction function = new AsgFunction(name, parameters, returnType);
        environment.declareFunction(function);
    }

    private AsgFunctionDefinition parseFunctionDefinition(GrammarParser.FunctionDefinitionContext ctx) {
        String name = ctx.name.getText();
        List<AsgType> parameterTypes = ctx.parameters().parameter().stream()
            .map(param -> param.type().accept(environment.asTypeParser()))
            .collect(Collectors.toList());
        AsgFunction declaration = environment.getFunction(name, parameterTypes);
        Context functionContext = new Context(environment, null);
        declaration.getParameters().forEach(functionContext::declareVariable);
        AsgStatement body = ctx.body.accept(functionContext.asStatementParser());
        return new AsgFunctionDefinition(declaration, body);
    }
}
