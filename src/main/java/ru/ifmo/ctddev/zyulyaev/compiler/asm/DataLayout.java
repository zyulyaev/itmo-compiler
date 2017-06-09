package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;

import java.util.Map;

/**
 * @author zyulyaev
 * @since 08.06.2017
 */
@Data
class DataLayout {
    private final AsgDataType dataType;
    private final int size;
    private final Map<AsgDataType.Field, Integer> fieldOffsetMap;

    int getFieldOffset(AsgDataType.Field field) {
        return fieldOffsetMap.get(field);
    }
}
