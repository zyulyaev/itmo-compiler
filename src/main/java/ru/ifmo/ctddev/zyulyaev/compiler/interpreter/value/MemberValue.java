package ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
@Data
public class MemberValue implements LeftValue {
    private final DataTypeValue object;
    private final AsgDataType.Field field;

    @Override
    public void set(RightValue value) {
        object.set(field, value);
    }

    @Override
    public RightValue asRightValue() {
        return object.get(field);
    }
}
