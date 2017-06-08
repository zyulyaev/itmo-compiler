package ru.ifmo.ctddev.zyulyaev.compiler.std;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.List;

/**
 * @author zyulyaev
 * @since 08.06.2017
 */
public class ArrlenFunction implements AsgExternalFunction {
    @Override
    public String getName() {
        return "arrlen";
    }

    @Override
    public int getParameterCount() {
        return 1;
    }

    @Override
    public AsgType resolveReturnType(List<AsgType> argumentTypes) {
        if (!argumentTypes.get(0).isArray()) {
            throw new IllegalArgumentException("array type expected");
        }
        return AsgPredefinedType.INT;
    }
}
