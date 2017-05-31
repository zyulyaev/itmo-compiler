package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.Runtime;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcArrayPointer;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcPointer;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcScalar;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcValue;

import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class BcRuntime extends Runtime<List<BcValue>, BcValue> {
    private final Scanner in;
    private final PrintWriter out;

    public BcRuntime(Scanner in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected BcValue readFunctionStub(List<BcValue> args) {
        out.print("> ");
        return new BcScalar(in.nextInt());
    }

    @Override
    protected BcValue writeFunctionStub(List<BcValue> args) {
        out.println(args.get(0).asScalar().getValue());
        return null;
    }

    @Override
    protected BcValue strlenFunctionStub(List<BcValue> args) {
        return new BcScalar(args.get(0).asPtr().length());
    }

    @Override
    protected BcValue strgetFunctionStub(List<BcValue> args) {
        return args.get(0).asPtr().shift(args.get(1).asScalar().getValue()).get();
    }

    @Override
    protected BcValue strsubFunctionStub(List<BcValue> args) {
        BcPointer ptr = args.get(0).asPtr();
        int start = args.get(1).asScalar().getValue();
        int length = args.get(2).asScalar().getValue();
        BcValue[] result = new BcValue[length];
        for (int i = 0; i < length; i++) {
            result[i] = ptr.shift(start + i).get();
        }
        return new BcArrayPointer(result, 0);
    }

    @Override
    protected BcValue strsetFunctionStub(List<BcValue> args) {
        args.get(0).asPtr().shift(args.get(1).asScalar().getValue()).set(args.get(2));
        return null;
    }

    @Override
    protected BcValue strcatFunctionStub(List<BcValue> args) {
        BcPointer left = args.get(0).asPtr();
        BcPointer right = args.get(1).asPtr();
        BcValue[] result = new BcValue[left.length() + right.length()];
        for (int i = 0; i < left.length(); i++) {
            result[i] = left.shift(i).get();
        }
        for (int i = 0; i < right.length(); i++) {
            result[left.length() + i] = right.shift(i).get();
        }
        return new BcArrayPointer(result, 0);
    }

    @Override
    protected BcValue strcmpFunctionStub(List<BcValue> args) {
        BcPointer left = args.get(0).asPtr();
        BcPointer right = args.get(1).asPtr();
        for (int i = 0; i < Math.max(left.length(), right.length()); i++) {
            if (i >= left.length()) {
                return new BcScalar(-1);
            }
            if (i >= right.length()) {
                return new BcScalar(1);
            }
            int cmp = Integer.compare(left.shift(i).get().asScalar().getValue(),
                right.shift(i).get().asScalar().getValue());
            if (cmp != 0) {
                return new BcScalar(cmp > 0 ? 1 : -1);
            }
        }
        return new BcScalar(0);
    }

    @Override
    protected BcValue strdupFunctionStub(List<BcValue> args) {
        BcPointer pointer = args.get(0).asPtr();
        BcValue[] result = new BcValue[pointer.length()];
        for (int i = 0; i < pointer.length(); i++) {
            result[i] = pointer.shift(i).get();
        }
        return new BcArrayPointer(result, 0);
    }

    @Override
    protected BcValue strmakeFunctionStub(List<BcValue> args) {
        int size = args.get(0).asScalar().getValue();
        BcValue value = args.get(1);
        return new BcArrayPointer(Stream.generate(() -> value).limit(size).toArray(BcValue[]::new), 0);
    }

    @Override
    protected BcValue arrlenFunctionStub(List<BcValue> args) {
        return new BcScalar(args.get(0).asPtr().length());
    }

    @Override
    protected BcValue arrmakeFunctionStub(List<BcValue> args) {
        int size = args.get(0).asScalar().getValue();
        BcValue value = args.get(1);
        return new BcArrayPointer(Stream.generate(() -> value).limit(size).toArray(BcValue[]::new), 0);
    }

    @Override
    protected BcValue ArrmakeFunctionStub(List<BcValue> args) {
        int size = args.get(0).asScalar().getValue();
        BcValue value = args.get(1);
        return new BcArrayPointer(Stream.generate(() -> value).limit(size).toArray(BcValue[]::new), 0);
    }
}
