package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import lombok.NonNull;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmOperand;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmPointer;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmSymbol;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @author zyulyaev
 * @since 09.06.2017
 */
class FunctionContext {
    static final List<AsmRegister> REGISTER_STACK = Arrays.asList(
        AsmRegister.EBX, AsmRegister.EDI, AsmRegister.ESI
    );

    private final Map<AsgVariable, AsmPointer> variables = new HashMap<>();
    private final Set<AsmRegister> usedRegisters = new HashSet<>();
    private final Queue<AsmPointer> stackPool = new ArrayDeque<>();
    private int allocatedOnStack;

    private final Map<BcLabel, AsmSymbol> lineLabels;
    private final AsmSymbol cleanupLabel;

    FunctionContext(AsgVariable thisValue, List<AsgVariable> parameters, List<AsgVariable> localVariables,
        Map<BcLabel, AsmSymbol> lineLabels, AsmSymbol cleanupLabel, int storedRegisters)
    {
        this.lineLabels = lineLabels;
        this.cleanupLabel = cleanupLabel;

        int parametersOffset = 8 + 4 * storedRegisters;
        if (thisValue != null) {
            variables.put(thisValue, new AsmPointer(AsmRegister.EBP, parametersOffset));
            parametersOffset += 4;
        }
        for (int i = 0; i < parameters.size(); i++) {
            AsgVariable variable = parameters.get(i);
            variables.put(variable, new AsmPointer(AsmRegister.EBP, 4 * i + parametersOffset));
        }
        for (int i = 0; i < localVariables.size(); i++) {
            AsgVariable variable = localVariables.get(i);
            variables.put(variable, new AsmPointer(AsmRegister.EBP, -4 * i - 4));
        }
        allocatedOnStack = localVariables.size();
    }

    AsmOperand allocate() {
        for (AsmRegister target : REGISTER_STACK) {
            if (usedRegisters.contains(target)) {
                continue;
            }
            usedRegisters.add(target);
            return target;
        }
        if (stackPool.isEmpty()) {
            stackPool.add(new AsmPointer(AsmRegister.EBP, -4 * allocatedOnStack - 4));
            allocatedOnStack++;
        }
        return stackPool.poll();
    }

    /**
     * Pools memory
     */
    void deallocate(@NonNull AsmOperand operand) {
        if (operand.isRegister()) {
            usedRegisters.remove(operand);
        } else {
            stackPool.add((AsmPointer) operand);
        }
    }

    AsmPointer getVariablePointer(AsgVariable variable) {
        return variables.get(variable);
    }

    int getStackSize() {
        return allocatedOnStack * 4;
    }

    AsmSymbol getLabel(BcLabel label) {
        return lineLabels.get(label);
    }

    AsmSymbol getCleanupLabel() {
        return cleanupLabel;
    }
}
