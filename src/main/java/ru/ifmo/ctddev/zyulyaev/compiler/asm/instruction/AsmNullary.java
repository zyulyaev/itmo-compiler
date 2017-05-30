package ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction;

/**
 * @author zyulyaev
 * @since 30.05.2017
 */
public enum AsmNullary implements AsmInstruction {
    NOP("nop"),
    RET("ret"),
    LEAVE("leave"),
    CLTD("cltd");

    private final String value;

    AsmNullary(String value) {
        this.value = value;
    }

    @Override
    public String print() {
        return value;
    }
}
