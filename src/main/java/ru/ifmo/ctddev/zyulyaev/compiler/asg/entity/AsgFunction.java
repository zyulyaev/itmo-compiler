package ru.ifmo.ctddev.zyulyaev.compiler.asg.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Getter
@AllArgsConstructor
@ToString
public class AsgFunction {
    private final String name;
    private final List<AsgVariable> parameters;
}
