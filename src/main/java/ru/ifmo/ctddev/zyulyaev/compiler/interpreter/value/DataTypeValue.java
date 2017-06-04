package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;

import java.util.Map;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
@Data
public class DataTypeValue implements RightValue {
    private final AsgDataType type;
    private final Map<AsgDataType.Field, RightValue> fields;

    public RightValue get(AsgDataType.Field field) {
        return fields.get(field);
    }

    public void set(AsgDataType.Field field, RightValue value) {
        fields.put(field, value);
    }
}
