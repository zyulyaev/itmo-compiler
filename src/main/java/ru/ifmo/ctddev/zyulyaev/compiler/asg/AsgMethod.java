package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.List;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class AsgMethod {
    private final AsgClassDefinition parent;
    private final String name;
    private final List<AsgVariable> parameters;
    private final AsgType returnType;
}
