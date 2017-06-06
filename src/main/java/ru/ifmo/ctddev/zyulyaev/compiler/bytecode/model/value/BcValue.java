package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 05.06.2017
 */
public interface BcValue {
    AsgType getType();

    <T> T accept(BcValueVisitor<T> visitor);
}
