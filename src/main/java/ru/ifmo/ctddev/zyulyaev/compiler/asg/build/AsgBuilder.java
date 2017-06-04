package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.GrammarBaseVisitor;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgImplDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethodDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
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

        List<AsgDataType> definedTypes =
            dataDefinitions.stream().map(this::parseDataDeclaration).collect(Collectors.toList());
        List<AsgClassType> definedClasses =
            classDefinitions.stream().map(this::parseClassDeclaration).collect(Collectors.toList());

        dataDefinitions.forEach(this::parseDataDefinition);
        classDefinitions.forEach(this::parseClassDefinition);

        functionDefinitions.forEach(this::parseFunctionDeclaration);
        List<AsgFunctionDefinition> definedFunctions =
            functionDefinitions.stream().map(this::parseFunctionDefinition).collect(Collectors.toList());

        List<AsgImplDefinition> definedImplementations =
            implDefinitions.stream().map(this::parseImplementation).collect(Collectors.toList());

        definedTypes.forEach(type -> type.setImplementedClasses(new ArrayList<>()));
        for (AsgImplDefinition impl : definedImplementations) {
            impl.getDataType().getImplementedClasses().add(impl.getClassType());
        }

        Context mainContext = new Context(environment, null);
        AsgStatement mainBody = program.statements().accept(mainContext.asStatementParser());

        return new AsgProgram(
            definedFunctions,
            definedTypes,
            definedClasses,
            definedImplementations,
            environment.getExternalFunctions(),
            mainBody
        );
    }

    private AsgDataType parseDataDeclaration(GrammarParser.DataDefinitionContext ctx) {
        AsgDataType type = new AsgDataType(ctx.name.getText());
        environment.defineType(type);
        return type;
    }

    private void parseDataDefinition(GrammarParser.DataDefinitionContext ctx) {
        AsgDataType type = (AsgDataType) environment.getType(ctx.name.getText());
        type.setFields(ctx.fields().field().stream()
            .map(field -> new AsgDataType.Field(field.name.getText(), field.accept(environment.asTypeParser())))
            .collect(Collectors.toList()));
    }

    private AsgClassType parseClassDeclaration(GrammarParser.ClassDefinitionContext ctx) {
        AsgClassType type = new AsgClassType(ctx.name.getText());
        environment.defineType(type);
        return type;
    }

    private void parseClassDefinition(GrammarParser.ClassDefinitionContext ctx) {
        AsgClassType type = (AsgClassType) environment.getType(ctx.name.getText());
        List<AsgMethod> methods = ctx.methodDecl().stream()
            .map(method -> new AsgMethod(
                type,
                method.name.getText(),
                method.parameters().parameter().stream()
                    .map(param -> param.type().accept(environment.asTypeParser()))
                    .collect(Collectors.toList()),
                method.returnType.accept(environment.asTypeParser())
            ))
            .collect(Collectors.toList());
        type.setMethods(methods);
    }

    private void parseFunctionDeclaration(GrammarParser.FunctionDefinitionContext ctx) {
        String name = ctx.name.getText();
        List<AsgType> parameterTypes = ctx.parameters().parameter().stream()
            .map(param -> param.type().accept(environment.asTypeParser()))
            .collect(Collectors.toList());
        AsgType returnType = ctx.returnType.accept(environment.asTypeParser());
        AsgFunction function = new AsgFunction(name, parameterTypes, returnType);
        environment.declareFunction(function);
    }

    private AsgFunctionDefinition parseFunctionDefinition(GrammarParser.FunctionDefinitionContext ctx) {
        String name = ctx.name.getText();
        List<AsgType> parameterTypes = ctx.parameters().parameter().stream()
            .map(param -> param.type().accept(environment.asTypeParser()))
            .collect(Collectors.toList());
        AsgFunction declaration = environment.getFunction(name, parameterTypes);
        Context functionContext = new Context(environment, null);
        List<AsgVariable> parameters = ctx.parameters().parameter().stream()
            .map(param -> functionContext.declareVariable(
                param.id().getText(),
                param.type().accept(environment.asTypeParser()),
                false
            ))
            .collect(Collectors.toList());
        AsgStatement body = ctx.body.accept(functionContext.asStatementParser());
        return new AsgFunctionDefinition(declaration, parameters, body);
    }

    private AsgImplDefinition parseImplementation(GrammarParser.ImplDefinitionContext ctx) {
        AsgClassType classType = (AsgClassType) environment.getType(ctx.className.getText());
        AsgDataType dataType = (AsgDataType) environment.getType(ctx.dataType.getText());
        List<AsgMethodDefinition> definitions = ctx.functionDefinition().stream()
            .map(def -> parseMethodDefinition(def, dataType, classType))
            .collect(Collectors.toList());
        return new AsgImplDefinition(classType, dataType, definitions);
    }

    private AsgMethodDefinition parseMethodDefinition(GrammarParser.FunctionDefinitionContext ctx, AsgDataType dataType,
        AsgClassType classType)
    {
        AsgMethod method = classType.getMethods().stream().filter(m -> m.getName().equals(ctx.name.getText()))
            .findFirst().orElseThrow(() -> new NoSuchElementException("Method " + ctx.name.getText() + " not found"));
        Context methodContext = new Context(environment, null);
        AsgVariable thisValue = methodContext.declareVariable("this", dataType, true);
        List<AsgVariable> parameters = ctx.parameters().parameter().stream()
            .map(param -> methodContext.declareVariable(
                param.id().getText(),
                param.type().accept(environment.asTypeParser()),
                false)
            )
            .collect(Collectors.toList());
        AsgStatement body = ctx.body.accept(methodContext.asStatementParser());
        return new AsgMethodDefinition(method, thisValue, parameters, body);
    }
}
