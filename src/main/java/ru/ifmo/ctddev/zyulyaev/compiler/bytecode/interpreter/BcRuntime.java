package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.Runtime;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcArrayPtrValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcIntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcValue;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class BcRuntime extends Runtime<List<BcValue>, BcValue> {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    protected BcValue readFunctionStub(List<BcValue> args) {
        System.out.print("> ");
        return new BcIntValue(scanner.nextInt());
    }

    @Override
    protected BcValue writeFunctionStub(List<BcValue> args) {
        System.out.println(args.get(0).asInt().getValue());
        return null;
    }

    @Override
    protected BcValue strlenFunctionStub(List<BcValue> args) {
        return new BcIntValue(args.get(0).asPtr().getValues().length);
    }

    @Override
    protected BcValue strgetFunctionStub(List<BcValue> args) {
        BcArrayPtrValue ptr = args.get(0).asPtr();
        int index = args.get(1).asInt().getValue();
        return new BcIntValue(ptr.getValues()[index].asInt().getValue());
    }

    @Override
    protected BcValue strsubFunctionStub(List<BcValue> args) {
        BcArrayPtrValue ptr = args.get(0).asPtr();
        int start = args.get(1).asInt().getValue();
        int length = args.get(2).asInt().getValue();
        return new BcArrayPtrValue(Arrays.copyOfRange(ptr.getValues(), start, start + length), 0);
    }

    @Override
    protected BcValue strsetFunctionStub(List<BcValue> args) {
        BcValue[] values = args.get(0).asPtr().getValues();
        int index = args.get(1).asInt().getValue();
        BcValue value = args.get(2);
        values[index] = value;
        return null;
    }

    @Override
    protected BcValue strcatFunctionStub(List<BcValue> args) {
        BcValue[] left = args.get(0).asPtr().getValues();
        BcValue[] right = args.get(1).asPtr().getValues();
        BcValue[] result = Arrays.copyOf(left, left.length + right.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return new BcArrayPtrValue(result, 0);
    }

    @Override
    protected BcValue strcmpFunctionStub(List<BcValue> args) {
        BcValue[] left = args.get(0).asPtr().getValues();
        BcValue[] right = args.get(1).asPtr().getValues();
        for (int i = 0; i < Math.max(left.length, right.length); i++) {
            if (i >= left.length) {
                return new BcIntValue(-1);
            }
            if (i >= right.length) {
                return new BcIntValue(1);
            }
            int cmp = Integer.compare(left[i].asInt().getValue(), right[i].asInt().getValue());
            if (cmp != 0) {
                return new BcIntValue(cmp > 0 ? 1 : -1);
            }
        }
        return new BcIntValue(0);
    }

    @Override
    protected BcValue strdupFunctionStub(List<BcValue> args) {
        BcValue[] values = args.get(0).asPtr().getValues();
        return new BcArrayPtrValue(values.clone(), 0);
    }

    @Override
    protected BcValue strmakeFunctionStub(List<BcValue> args) {
        int size = args.get(0).asInt().getValue();
        BcValue value = args.get(1);
        return new BcArrayPtrValue(Stream.generate(() -> value).limit(size).toArray(BcValue[]::new), 0);
    }

    @Override
    protected BcValue arrlenFunctionStub(List<BcValue> args) {
        return new BcIntValue(args.get(0).asPtr().getValues().length);
    }

    @Override
    protected BcValue arrmakeFunctionStub(List<BcValue> args) {
        int size = args.get(0).asInt().getValue();
        BcValue value = args.get(1);
        return new BcArrayPtrValue(Stream.generate(() -> value).limit(size).toArray(BcValue[]::new), 0);
    }

    @Override
    protected BcValue ArrmakeFunctionStub(List<BcValue> args) {
        int size = args.get(0).asInt().getValue();
        BcValue value = args.get(1);
        return new BcArrayPtrValue(Stream.generate(() -> value).limit(size).toArray(BcValue[]::new), 0);
    }
}
