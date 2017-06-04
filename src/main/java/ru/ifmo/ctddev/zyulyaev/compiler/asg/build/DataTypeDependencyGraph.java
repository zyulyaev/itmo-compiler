package ru.ifmo.ctddev.zyulyaev.compiler.asg.build;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
class DataTypeDependencyGraph {
    private final Map<String, List<String>> edges;

    DataTypeDependencyGraph(List<Edge> edges) {
        this.edges = edges.stream().collect(Collectors.groupingBy(
            Edge::getDependant,
            Collectors.mapping(Edge::getDependency, Collectors.toList())
        ));
    }

    Optional<List<String>> findCycle() {
        Map<String, Integer> state = new HashMap<>();
        for (String type : edges.keySet()) {
            List<String> cycle = findCycle(type, state, new ArrayList<>());
            if (cycle != null) {
                return Optional.of(cycle);
            }
        }
        return Optional.empty();
    }

    List<String> topologySort() {
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (String v : edges.keySet()) {
            topologySort(v, visited, result);
        }
        return result;
    }

    private void topologySort(String v, Set<String> visited, List<String> result) {
        if (visited.contains(v)) {
            return;
        }
        visited.add(v);
        for (String to : edges.getOrDefault(v, Collections.emptyList())) {
            topologySort(to, visited, result);
        }
        result.add(v);
    }

    private List<String> findCycle(String v, Map<String, Integer> state, List<String> stack) {
        Integer currentState = state.getOrDefault(v, 0);
        if (currentState == 1) {
            return stack.subList(stack.indexOf(v), stack.size());
        }
        if (currentState == 2) {
            return null;
        }
        state.put(v, 1);
        stack.add(v);
        for (String to : edges.getOrDefault(v, Collections.emptyList())) {
            List<String> cycle = findCycle(to, state, stack);
            if (cycle != null) {
                return cycle;
            }
        }
        state.put(v, 2);
        stack.remove(stack.size() - 1);
        return null;
    }

    @Data
    static class Edge {
        private final String dependant;
        private final String dependency;
    }
}
