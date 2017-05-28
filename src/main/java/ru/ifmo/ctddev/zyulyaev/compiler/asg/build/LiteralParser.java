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
        return new AsgLiteralExpression<>(AsgLiteralExpression.LiteralType.STRING, parseLiteral(ctx.getText()));
    }

    @Override
    public AsgLiteralExpression<?> visitIntLiteral(GrammarParser.IntLiteralContext ctx) {
        return new AsgLiteralExpression<>(AsgLiteralExpression.LiteralType.INT, Integer.parseInt(ctx.getText()));
    }

    @Override
    public AsgLiteralExpression<?> visitCharLiteral(GrammarParser.CharLiteralContext ctx) {
        return new AsgLiteralExpression<>(AsgLiteralExpression.LiteralType.INT, parseLiteral(ctx.getText()).codePointAt(0));
    }

    @Override
    public AsgLiteralExpression<?> visitBoolLiteral(GrammarParser.BoolLiteralContext ctx) {
        return new AsgLiteralExpression<>(AsgLiteralExpression.LiteralType.INT, "true".equals(ctx.getText()) ? 1 : 0);
    }

    @Override
    public AsgLiteralExpression<?> visitNullLiteral(GrammarParser.NullLiteralContext ctx) {
        return new AsgLiteralExpression<>(AsgLiteralExpression.LiteralType.NULL, null);
    }

    private String parseLiteral(String text) {
        return text.substring(1, text.length() - 1).replaceAll("\\\\(.)", "$1");
    }
}
