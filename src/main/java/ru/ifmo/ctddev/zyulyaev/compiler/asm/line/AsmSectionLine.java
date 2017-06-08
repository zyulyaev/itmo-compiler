package ru.ifmo.ctddev.zyulyaev.compiler.asm.line;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public enum AsmSectionLine implements AsmLine {
    TEXT,
    DATA;

    @Override
    public String print() {
        return "." + this.name().toLowerCase();
    }
}
