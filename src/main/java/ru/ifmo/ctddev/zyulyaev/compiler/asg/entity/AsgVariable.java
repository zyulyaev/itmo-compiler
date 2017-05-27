package ru.ifmo.ctddev.zyulyaev.compiler.asg.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Getter
@AllArgsConstructor
@ToString
public class AsgVariable {
    private final String name;
}
