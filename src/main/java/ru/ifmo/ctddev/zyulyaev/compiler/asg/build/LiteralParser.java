package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.GrammarBaseVisitor;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLiteralExpression;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
class LiteralParser extends GrammarBaseVisitor<AsgLiteralExpression<?>> {
    @Override
    public AsgLiteralExpression<?> visitStringLiteral(GrammarParser.StringLiteralContext ctx) {
        return new AsgLiteralExpression<>(AsgLiteralExpression.LiteralType.STRING, ctx.getText());
    }

    @Override
    public AsgLiteralExpression<?> visitIntLiteral(GrammarParser.IntLiteralContext ctx) {
        return new AsgLiteralExpression<>(AsgLiteralExpression.LiteralType.INT, Integer.parseInt(ctx.getText()));
    }
}
