package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.GrammarBaseVisitor;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgArrayType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
class TypeParser extends GrammarBaseVisitor<AsgType> {
    private final Environment environment;

    TypeParser(Environment environment) {
        this.environment = environment;
    }

    @Override
    public AsgType visitPlainType(GrammarParser.PlainTypeContext ctx) {
        return environment.getType(ctx.getText());
    }

    @Override
    public AsgType visitArrayType(GrammarParser.ArrayTypeContext ctx) {
        return new AsgArrayType(ctx.type().accept(this));
    }
}
