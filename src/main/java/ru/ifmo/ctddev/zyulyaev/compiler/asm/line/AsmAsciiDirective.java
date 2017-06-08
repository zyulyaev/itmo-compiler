package ru.ifmo.ctddev.zyulyaev.compiler.asm.line;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 07.06.2017
 */
@Data
public class AsmAsciiDirective implements AsmLine {
    private final String value;

    @Override
    public String print() {
        return ".ascii \"" + value.replaceAll("[\\\\\"]", "\\$1") + "\\0\"";
    }
}
