package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class AsgClassDefinition {
    private final String name;
    private final List<AsgMethod> methods;
}
