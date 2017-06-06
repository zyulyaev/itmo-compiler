package ru.ifmo.ctddev.zyulyaev.compiler.asg;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public enum AsgBinaryOperator {
    // by precedence
    MUL("*"), DIV("/"), MOD("%"),
    ADD("+"), SUB("-"),
    LT("<"), GT(">"), LTE("<="), GTE(">="), EQ("=="), NEQ("!="),
    AND("&&"), OR("!!");

    private final String textual;

    AsgBinaryOperator(String textual) {
        this.textual = textual;
    }

    public String getTextual() {
        return textual;
    }
}
