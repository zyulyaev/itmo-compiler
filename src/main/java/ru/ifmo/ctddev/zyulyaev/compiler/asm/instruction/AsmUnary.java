package ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmOperand;

/**
 * @author zyulyaev
 * @since 30.05.2017
 */
public enum AsmUnary {
    DIV("idivl"),
    PUSH("pushl"),
    POP("popl"),
    CALL("call"),
    INC("incl"),
    DEC("decl"),

    JMP("jmp"),
    JZ("jz"),
    JNZ("jnz"),
    JGE("jge"),

    /** if equal (or zero) */
    SETE("sete"),
    /** if not equal (or non zero) */
    SETNE("setne"),
    /** if greater */
    SETG("setg"),
    /** if greater or equal */
    SETGE("setge"),
    /** if less */
    SETL("setl"),
    /** if less or equal */
    SETLE("setle");


    private final String value;

    AsmUnary(String value) {
        this.value = value;
    }

    public AsmInstruction create(AsmOperand operand) {
        return new AsmUnaryInstruction(this, operand);
    }

    @Data
    private static class AsmUnaryInstruction implements AsmInstruction {
        private final AsmUnary operator;
        private final AsmOperand operand;

        @Override
        public String print() {
            if (operator == CALL && !operand.isSymbol()) {
                return "call *" + operand.print();
            }
            return operator.value + " " + operand.print();
        }
    }
}
