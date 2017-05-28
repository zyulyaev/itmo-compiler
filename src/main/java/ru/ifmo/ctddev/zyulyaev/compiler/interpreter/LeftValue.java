package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.entity.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;

import java.util.List;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class LeftValue {
    private final AsgVariable variable;
    private final List<IntValue> indexes;

    public boolean isPlainVariable() {
        return indexes.isEmpty();
    }
}
