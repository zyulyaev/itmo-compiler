package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLine;

import java.util.List;

/**
 * @author zyulyaev
 * @since 06.06.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class BcMethodDefinition {
    private final AsgDataType dataType;
    private final AsgMethod method;
    private final AsgVariable thisValue;
    private final List<AsgVariable> parameters;
    private final List<AsgVariable> localVariables;
    private final List<BcLine> body;
}
