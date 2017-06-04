package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;

import java.util.List;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class AsgMethodDefinition {
    private final AsgMethod method;
    private final AsgVariable thisValue;
    private final List<AsgVariable> parameters;
    private final AsgStatement body;
}
