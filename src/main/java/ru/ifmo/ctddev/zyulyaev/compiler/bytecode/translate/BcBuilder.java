package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcInstructionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
class BcBuilder {
    private final List<BcLine> lines = new ArrayList<>();
    private final Map<String, Integer> labelCounters = new HashMap<>();
    private final Set<AsgVariable> localVariables = new LinkedHashSet<>();
    private int registerCounter = 0;

    private final Set<AsgVariable> predefined;

    BcBuilder(Set<AsgVariable> predefined) {
        this.predefined = predefined;
    }

    BcRegister write(BcInstruction instruction) {
        if (instruction.getResultType() == AsgPredefinedType.NONE) {
            lines.add(new BcInstructionLine(null, instruction));
            return null;
        } else {
            BcRegister register = new BcRegister(registerCounter++, instruction.getResultType());
            lines.add(new BcInstructionLine(register, instruction));
            return register;
        }
    }

    void write(BcLabel label) {
        BcLine line = new BcLabelLine(label);
        lines.add(line);
    }

    void useVariable(AsgVariable variable) {
        if (!predefined.contains(variable)) {
            localVariables.add(variable);
        }
    }

    BcLabel reserveLabel(String name) {
        int counter = labelCounters.merge(name, 1, Integer::sum);
        return new BcLabel("l_" + name + "_" + counter);
    }

    List<BcLine> getLines() {
        return lines;
    }

    Set<AsgVariable> getLocalVariables() {
        return localVariables;
    }
}
