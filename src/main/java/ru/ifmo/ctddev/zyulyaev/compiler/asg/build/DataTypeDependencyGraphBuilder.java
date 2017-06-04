package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.GrammarBaseVisitor;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
class DataTypeDependencyGraphBuilder {
    static DataTypeDependencyGraph build(List<GrammarParser.DataDefinitionContext> ctxs) {
        List<DataTypeDependencyGraph.Edge> edges = ctxs.stream()
            .flatMap(DataTypeDependencyGraphBuilder::buildEdges)
            .collect(Collectors.toList());
        return new DataTypeDependencyGraph(edges);
    }

    private static Stream<DataTypeDependencyGraph.Edge> buildEdges(GrammarParser.DataDefinitionContext ctx) {
        return ctx.accept(new GrammarBaseVisitor<Stream<DataTypeDependencyGraph.Edge>>() {
            @Override
            public Stream<DataTypeDependencyGraph.Edge> visitDataDefinition(GrammarParser.DataDefinitionContext ctx) {
                String dataTypeName = ctx.name.getText();
                return ctx.fields().field().stream()
                    .flatMap(DataTypeDependencyGraphBuilder::buildDependencies)
                    .map(dependency -> new DataTypeDependencyGraph.Edge(dataTypeName, dependency));
            }

            @Override
            protected Stream<DataTypeDependencyGraph.Edge> defaultResult() {
                return Stream.empty();
            }

            @Override
            protected Stream<DataTypeDependencyGraph.Edge> aggregateResult(Stream<DataTypeDependencyGraph.Edge> aggregate,
                Stream<DataTypeDependencyGraph.Edge> nextResult)
            {
                return Stream.concat(aggregate, nextResult);
            }
        });
    }

    private static Stream<String> buildDependencies(GrammarParser.FieldContext field) {
        return field.type().accept(new GrammarBaseVisitor<Stream<String>>() {
            @Override
            public Stream<String> visitPlainType(GrammarParser.PlainTypeContext ctx) {
                return Stream.of(ctx.id().getText());
            }

            @Override
            public Stream<String> visitArrayType(GrammarParser.ArrayTypeContext ctx) {
                return ctx.type().accept(this);
            }

            @Override
            protected Stream<String> defaultResult() {
                return Stream.empty();
            }

            @Override
            protected Stream<String> aggregateResult(Stream<String> aggregate, Stream<String> nextResult) {
                return Stream.concat(aggregate, nextResult);
            }
        });
    }
}
