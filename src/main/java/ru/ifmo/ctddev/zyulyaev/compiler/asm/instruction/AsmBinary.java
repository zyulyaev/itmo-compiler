package ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmOperand;

/**
 * @author zyulyaev
 * @since 30.05.2017
 */
public enum AsmBinary {
    ADD("addl"),
    SUB("subl"),
    MUL("imull"),

    XOR("xorl"),
    OR("orl"),
    AND("andl"),
    TEST("test"),

    MOV("movl"),
    LEA("lea"),
    CMP("cmp"),
    ENTER("enter");

    private final String value;

    AsmBinary(String value) {
        this.value = value;
    }

    public AsmInstruction create(AsmOperand dest, AsmOperand src) {
        return new AsmBinaryInstruction(this, dest, src);
    }

    @Data
    private static class AsmBinaryInstruction implements AsmInstruction {
        private final AsmBinary operator;
        private final AsmOperand dest;
        private final AsmOperand src;

        @Override
        public String print() {
            return operator.value + " " + src.print() + ", " + dest.print();
        }
    }
}
