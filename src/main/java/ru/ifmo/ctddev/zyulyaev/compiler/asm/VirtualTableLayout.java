package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;

import java.util.Map;

/**
 * @author zyulyaev
 * @since 08.06.2017
 */
@Data
class VirtualTableLayout {
    private final AsgClassType classType;
    private final Map<AsgMethod, Integer> methodOffsetMap;

    int getMethodOffset(AsgMethod method) {
        return methodOffsetMap.get(method);
    }
}
