package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value;

/**
 * @author zyulyaev
 * @since 05.06.2017
 */
public interface BcValueVisitor<T> {
    T visit(BcImmediateValue value);

    T visit(BcNoneValue value);

    T visit(BcRegister register);
}
