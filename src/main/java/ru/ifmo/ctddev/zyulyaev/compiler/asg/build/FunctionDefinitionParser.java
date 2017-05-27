package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.GrammarBaseVisitor;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class FunctionDefinitionParser extends GrammarBaseVisitor<AsgFunctionDefinition> {
    private final Context context;

    FunctionDefinitionParser(Context context) {
        this.context = context;
    }

    @Override
    public AsgFunctionDefinition visitFunctionDefinition(GrammarParser.FunctionDefinitionContext ctx) {
        String name = ctx.name.getText();
        Context functionContext = context.createChild();
        List<AsgVariable> parameters = ctx.parameters().parameter().stream()
            .map(param -> functionContext.declareVariable(param.getText()))
            .collect(Collectors.toList());
        AsgFunction function = context.declareFunction(name, parameters);
        AsgStatement body = ctx.body.accept(functionContext.asStatementParser());
        return new AsgFunctionDefinition(function, body);
    }
}
