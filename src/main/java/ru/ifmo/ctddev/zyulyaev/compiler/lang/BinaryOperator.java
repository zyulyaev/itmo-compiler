package ru.ifmo.ctddev.zyulyaev.compiler.lang;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public enum BinaryOperator {
    // by precedence
    MUL, DIV, MOD,
    ADD, SUB,
    LT, GT, LTE, GTE, EQ, NEQ,
    AND, OR, WAT
}
