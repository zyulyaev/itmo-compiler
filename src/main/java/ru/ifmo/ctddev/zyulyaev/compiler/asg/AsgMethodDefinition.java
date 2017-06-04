package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class AsgMethodDefinition {
    private final AsgMethod method;
    private final AsgStatement body;
}
