package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public enum BcNullaryInstructions implements BcInstruction {
    NOP {
        @Override
        public <T> T accept(BcInstructionVisitor<T> visitor) {
            return visitor.visitNop(this);
        }
    },
    POP {
        @Override
        public <T> T accept(BcInstructionVisitor<T> visitor) {
            return visitor.visitPop(this);
        }
    },
    LOAD {
        @Override
        public <T> T accept(BcInstructionVisitor<T> visitor) {
            return visitor.visitLoad(this);
        }
    },
    STORE {
        @Override
        public <T> T accept(BcInstructionVisitor<T> visitor) {
            return visitor.visitStore(this);
        }
    },
    RETURN {
        @Override
        public <T> T accept(BcInstructionVisitor<T> visitor) {
            return visitor.visitReturn(this);
        }
    }
}
