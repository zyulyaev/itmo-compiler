package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import lombok.Getter;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmOperand;

/**
 * @author zyulyaev
 * @since 09.06.2017
 */
@Getter
class VirtualRegisterValue {
    private final AsgType type;
    private final AsmOperand main;
    private final AsmOperand aux;

    VirtualRegisterValue(AsgType type, AsmOperand main, AsmOperand aux) {
        this.type = type;
        this.main = main;
        this.aux = aux;
    }

    VirtualRegisterValue(AsgType type, AsmOperand main) {
        this(type, main, null);
    }

    static VirtualRegisterValue of(AsgType type, AsmOperand main, AsmOperand aux) {
        if (type.isClass()) {
            return new VirtualRegisterValue(type, main, aux);
        } else {
            return new VirtualRegisterValue(type, main);
        }
    }

    boolean hasAux() {
        return aux != null;
    }
}
