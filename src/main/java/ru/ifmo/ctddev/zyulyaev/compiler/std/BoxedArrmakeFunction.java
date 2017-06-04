package ru.ifmo.ctddev.zyulyaev.compiler.std;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgArrayType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.List;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
public class BoxedArrmakeFunction implements AsgExternalFunction {
    @Override
    public String getName() {
        return "Arrmake";
    }

    @Override
    public int getParameterCount() {
        return 2;
    }

    @Override
    public AsgType resolveReturnType(List<AsgType> argumentTypes) {
        if (argumentTypes.get(0) != AsgPredefinedType.INT) {
            throw new IllegalArgumentException("First Int parameter expected but got: " + argumentTypes.get(0));
        }
        AsgType compoundType = argumentTypes.get(1);
        if (compoundType.isPrimitive()) {
            throw new IllegalArgumentException("Use arrmake for primitive arrays");
        }
        return new AsgArrayType(compoundType);
    }
}
