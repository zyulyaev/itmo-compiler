package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;

import java.util.Map;

/**
 * @author zyulyaev
 * @since 06.06.2017
 */
@Data
class DataValue implements Value {
    private final AsgDataType dataType;
    private final Map<AsgDataType.Field, Value> values;

    Value get(AsgDataType.Field field) {
        return values.get(field);
    }

    void set(AsgDataType.Field field, Value value) {
        values.put(field, value);
    }

    @Override
    public ValueType getType() {
        return ValueType.DATA;
    }
}
