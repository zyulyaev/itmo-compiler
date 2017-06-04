package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
class Environment {
    private final Map<String, AsgType> types = Arrays.stream(AsgPredefinedType.values())
        .collect(Collectors.toMap(AsgPredefinedType::getName, Function.identity()));
    private final Map<String, AsgFunction> functions = new HashMap<>();
    private final Map<String, AsgExternalFunction> externalFunctions;

    private final Map<ExternalFunctionCallSignature, AsgFunction> externalFunctionsCache = new HashMap<>();

    Environment(Collection<AsgExternalFunction> externalFunctions) {
        this.externalFunctions = externalFunctions.stream()
            .collect(Collectors.toMap(AsgExternalFunction::getName, Function.identity()));
    }

    Map<AsgFunction, AsgExternalFunction> getExternalFunctions() {
        return externalFunctionsCache.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getValue,
            entry -> entry.getKey().getFunction()
        ));
    }

    void defineType(AsgDataType type) {
        types.put(type.getName(), type);
    }

    void defineType(AsgClassType type) {
        types.put(type.getName(), type);
    }

    void declareFunction(AsgFunction function) {
        functions.put(function.getName(), function);
    }

    AsgFunction getFunction(String name, List<AsgType> argumentTypes) {
        if (externalFunctions.containsKey(name)) {
            AsgExternalFunction externalFunction = externalFunctions.get(name);
            return externalFunctionsCache.computeIfAbsent(
                new ExternalFunctionCallSignature(externalFunction, argumentTypes),
                sign -> new AsgFunction(
                    sign.function.getName(),
                    sign.argumentTypes,
                    sign.function.resolveReturnType(sign.argumentTypes)
                ));
        } else {
            AsgFunction function = functions.get(name);
            if (function.getParameterTypes().size() != argumentTypes.size()) {
                throw new IllegalArgumentException("Expected " + function.getParameterTypes() + " arguments but got: " +
                    argumentTypes.size());
            }
            for (int i = 0; i < function.getParameterTypes().size(); i++) {
                AsgType parameterType = function.getParameterTypes().get(i);
                AsgType argumentType = argumentTypes.get(i);
                if (!parameterType.isAssignableFrom(argumentType)) {
                    throw new IllegalArgumentException("Expected " + parameterType + " but got: " + argumentType);
                }
            }
            return function;
        }
    }

    AsgType getType(String name) {
        return types.get(name);
    }

    boolean containsType(String name) {
        return types.containsKey(name);
    }

    TypeParser asTypeParser() {
        return new TypeParser(this);
    }

    @Data
    private static class ExternalFunctionCallSignature {
        private final AsgExternalFunction function;
        private final List<AsgType> argumentTypes;
    }
}
