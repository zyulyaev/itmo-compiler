package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 06.06.2017
 */
@Data
class StringValue implements Value {
    private final char[] chars;

    int get(int index) {
        return chars[index];
    }

    void set(int index, int value) {
        chars[index] = (char) value;
    }

    int length() {
        return chars.length;
    }

    @Override
    public ValueType getType() {
        return ValueType.STRING;
    }
}
