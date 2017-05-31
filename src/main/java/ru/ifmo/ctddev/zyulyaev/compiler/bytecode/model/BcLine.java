package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public interface BcLine {
    <T> T accept(BcLineVisitor<T> visitor);
}
