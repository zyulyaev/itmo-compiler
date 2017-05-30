package ru.ifmo.ctddev.zyulyaev.compiler.asm.operand;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public enum AsmRegister implements AsmOperand {
    /** Accumulator */
    EAX,
    AX,
    AL,
    AH,

    /** Counter */
    ECX,
    /** Data */
    EDX,
    /** Base */
    EBX,
    /** Stack pointer */
    ESP,
    /** Stack base pointer */
    EBP,
    /** Source */
    ESI,
    /** Destination */
    EDI;

    public String print() {
        return "%" + this.name().toLowerCase();
    }
}
