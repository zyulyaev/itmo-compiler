package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;

import java.util.List;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class AsgImplDefinition {
    private final AsgClassType classType;
    private final AsgDataType dataType;
    private List<AsgMethodDefinition> definitions;

    public AsgMethodDefinition getDefinition(AsgMethod method) {
        return definitions.stream()
            .filter(def -> def.getMethod().equals(method))
            .findFirst().orElse(null);
    }
}
