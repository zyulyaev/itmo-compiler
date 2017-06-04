package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Getter
@AllArgsConstructor
@ToString
public class AsgVariable {
    private final String name;
    private final AsgType type;
}
