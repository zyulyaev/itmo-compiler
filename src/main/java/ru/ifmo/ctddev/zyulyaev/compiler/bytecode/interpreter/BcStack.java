package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcPointer;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zyulyaev
 * @since 31.05.2017
 */
class BcStack {
    private final List<BcValue> data = new ArrayList<>();

    StackPointer push(BcValue value) {
        data.add(value);
        return new StackPointer(data.size() - 1);
    }

    StackPointer at(int depth) {
        return new StackPointer(data.size() - 1 - depth);
    }

    BcValue pop() {
        return data.remove(data.size() - 1);
    }

    List<BcValue> pop(int count) {
        int size = data.size();
        List<BcValue> subList = data.subList(size - count, size);
        List<BcValue> result = new ArrayList<>(subList);
        subList.clear();
        return result;
    }

    @Data
    private class StackPointer implements BcPointer {
        private final int index;

        @Override
        public void set(BcValue value) {
            data.set(index, value);
        }

        @Override
        public BcValue get() {
            return data.get(index);
        }

        @Override
        public BcPointer shift(int offset) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int length() {
            throw new UnsupportedOperationException();
        }
    }
}
