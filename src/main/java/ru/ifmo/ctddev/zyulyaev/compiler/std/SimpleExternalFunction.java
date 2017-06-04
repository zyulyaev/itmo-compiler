package ru.ifmo.ctddev.zyulyaev.compiler.std;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.Arrays;
import java.util.List;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
public class SimpleExternalFunction implements AsgExternalFunction {
    private final String name;
    private final AsgType returnType;
    private final List<AsgType> parameterTypes;

    public SimpleExternalFunction(String name, AsgType returnType, AsgType... parameterTypes) {
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = Arrays.asList(parameterTypes);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getParameterCount() {
        return parameterTypes.size();
    }

    @Override
    public AsgType resolveReturnType(List<AsgType> argumentTypes) {
        int parametersCount = parameterTypes.size();
        int argumentsCount = argumentTypes.size();
        if (parametersCount != argumentsCount) {
            throw new IllegalArgumentException("Expected " + parametersCount + " but got " + argumentsCount + " on " +
                name);
        }
        for (int i = 0; i < parametersCount; i++) {
            if (!parameterTypes.get(i).isAssignableFrom(argumentTypes.get(i))) {
                throw new IllegalArgumentException("Expected " + parameterTypes + " but got: " + argumentTypes +
                    " for function " + name);
            }
        }
        return returnType;
    }
}
