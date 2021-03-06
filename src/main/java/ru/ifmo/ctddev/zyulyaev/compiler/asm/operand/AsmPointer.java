package ru.ifmo.ctddev.zyulyaev.compiler.asm.operand;

import lombok.Data;

import javax.annotation.Nullable;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
@Data
public class AsmPointer implements AsmOperand {
    private final AsmRegister base;
    private final int factor;
    @Nullable
    private final AsmRegister spread;
    private final int offset;

    public AsmPointer(AsmRegister base, int factor, AsmRegister spread, int offset) {
        this.base = base;
        this.factor = factor;
        this.spread = spread;
        this.offset = offset;
    }

    public AsmPointer(AsmRegister base, int offset) {
        this(base, 0, null, offset);
    }

    @Override
    public String print() {
        if (spread != null) {
            return offset + "(" + base.print() + "," + spread.print() + "," + factor + ")";
        } else {
            return offset + "(" + base.print() + ")";
        }
    }

    private String formatInt(int value) {
        if (value < 0) {
            return Integer.toString(value);
        } else {
            return "+" + value;
        }
    }
}
