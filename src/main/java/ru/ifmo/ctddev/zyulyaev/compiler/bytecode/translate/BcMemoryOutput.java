package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstruction;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcMemoryOutput implements BcOutput {
    private BcMemoryLine head;
    private BcMemoryLine tail;

    @Override
    public BcLine write(BcInstruction instruction) {
        return pushLine(instruction, false);
    }

    @Override
    public BcDummy dummy() {
        return pushLine(null, true);
    }

    @Override
    public BcLine getStart() {
        return head;
    }

    private BcMemoryLine pushLine(BcInstruction instruction, boolean dummy) {
        tail = new BcMemoryLine(tail, null, instruction, dummy);
        if (tail.previous == null) {
            head = tail;
        } else {
            tail.previous.next = tail;
        }
        return tail;
    }

    private static class BcMemoryLine implements BcLine, BcDummy {
        private BcMemoryLine previous;
        private BcMemoryLine next;
        private BcInstruction instruction;
        private boolean dummy;

        public BcMemoryLine(BcMemoryLine previous, BcMemoryLine next, BcInstruction instruction, boolean dummy) {
            this.previous = previous;
            this.next = next;
            this.instruction = instruction;
            this.dummy = dummy;
        }

        @Override
        public BcLine replace(BcInstruction instruction) {
            if (!dummy) {
                throw new IllegalStateException("This line is not a dummy");
            }
            this.instruction = instruction;
            return this;
        }

        @Override
        public BcLine getNext() {
            return next;
        }

        @Override
        public BcInstruction getInstruction() {
            return instruction;
        }
    }
}
