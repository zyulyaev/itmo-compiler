package ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public interface AsgStatement {
    <T> T accept(AsgStatementVisitor<T> visitor);
}
