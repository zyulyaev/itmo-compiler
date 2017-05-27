package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

import lombok.Data;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class StringValue implements Value {
    private final String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public ValueType getType() {
        return ValueType.STRING;
    }

    @Override
    public String stringValue() {
        return value;
    }
}
