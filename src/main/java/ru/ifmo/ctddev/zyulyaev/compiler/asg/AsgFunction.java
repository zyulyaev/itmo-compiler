package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Getter
@AllArgsConstructor
public class AsgFunction {
    private final String name;
    private final List<AsgType> parameterTypes;
    private final AsgType returnType;

    @Override
    public String toString() {
        String params = parameterTypes.stream()
            .map(Object::toString)
            .collect(Collectors.joining(", "));
        return "function " + name + "(" + params + "): " + returnType;
    }
}
