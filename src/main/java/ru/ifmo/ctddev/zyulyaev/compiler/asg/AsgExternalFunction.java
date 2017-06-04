package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.List;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public interface AsgExternalFunction {
    String getName();

    int getParameterCount();

    AsgType resolveReturnType(List<AsgType> argumentTypes);
}
