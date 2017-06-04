package ru.ifmo.ctddev.zyulyaev.compiler.asg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class AsgImplDefinition {
    private final AsgClassDefinition classDefinition;
    private final AsgDataType dataType;
}
