package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author zyulyaev
 * @since 14-Jun-17
 */
class SuperClassCollector {
    private final Map<AsgClassType, State> stateMap = new HashMap<>();

    private SuperClassCollector() {}

    static void collectTransitiveSuperClasses(Collection<AsgClassType> types) {
        SuperClassCollector collector = new SuperClassCollector();
        types.forEach(collector::process);
    }

    private void process(AsgClassType type) {
        State state = stateMap.getOrDefault(type, State.UNPROCESSED);
        if (state == State.PROCESSING) {
            throw new IllegalStateException("Inheritance cycle detected");
        }
        if (state == State.PROCESSED) {
            return;
        }
        stateMap.put(type, State.PROCESSING);
        Set<AsgClassType> transitive = new HashSet<>();
        for (AsgClassType superClass : type.getSuperClasses()) {
            process(superClass);
            transitive.addAll(superClass.getSuperClasses());
        }
        type.getSuperClasses().addAll(transitive);
        stateMap.put(type, State.PROCESSED);
    }

    private enum State {
        UNPROCESSED,
        PROCESSING,
        PROCESSED
    }
}
