package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.Runtime;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.ArrayValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.NoneValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.RightValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.StringValue;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class InterpreterRuntime extends Runtime<List<RightValue>, RightValue> {
    private final Scanner in;
    private final PrintStream out;

    public InterpreterRuntime(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected RightValue readFunctionStub(List<RightValue> args) {
        out.print("> ");
        return new IntValue(in.nextInt());
    }

    @Override
    protected RightValue writeFunctionStub(List<RightValue> args) {
        out.println(args.get(0).asInt().getValue());
        return NoneValue.INSTANCE;
    }

    @Override
    protected RightValue strlenFunctionStub(List<RightValue> args) {
        return new IntValue(args.get(0).asString().getChars().length);
    }

    @Override
    protected RightValue strgetFunctionStub(List<RightValue> args) {
        return new IntValue(args.get(0).asString().getChars()[args.get(1).asInt().getValue()]);
    }

    @Override
    protected RightValue strsubFunctionStub(List<RightValue> args) {
        int start = args.get(1).asInt().getValue();
        int end = start + args.get(2).asInt().getValue();
        return new StringValue(Arrays.copyOfRange(args.get(0).asString().getChars(), start, end));
    }

    @Override
    protected RightValue strsetFunctionStub(List<RightValue> args) {
        StringValue value = args.get(0).asString();
        int idx = args.get(1).asInt().getValue();
        int ch = args.get(2).asInt().getValue();
        char[] chars = value.getChars();
        chars[idx] = (char) ch;
        return NoneValue.INSTANCE;
    }

    @Override
    protected RightValue strcatFunctionStub(List<RightValue> args) {
        char[] left = args.get(0).asString().getChars();
        char[] right = args.get(1).asString().getChars();
        char[] chars = new char[left.length + right.length];
        System.arraycopy(left, 0, chars, 0, left.length);
        System.arraycopy(right, 0, chars, left.length, right.length);
        return new StringValue(chars);
    }

    @Override
    protected RightValue strcmpFunctionStub(List<RightValue> args) {
        String left = new String(args.get(0).asString().getChars());
        String right = new String(args.get(1).asString().getChars());
        return new IntValue(left.compareTo(right));
    }

    @Override
    protected RightValue strdupFunctionStub(List<RightValue> args) {
        return new StringValue(args.get(0).asString().getChars().clone());
    }

    @Override
    protected RightValue strmakeFunctionStub(List<RightValue> args) {
        int length = args.get(0).asInt().getValue();
        char[] chars = new char[length];
        Arrays.fill(chars, (char) args.get(1).asInt().getValue());
        return new StringValue(chars);
    }

    @Override
    protected RightValue arrlenFunctionStub(List<RightValue> args) {
        return new IntValue(args.get(0).asArray().length());
    }

    @Override
    protected RightValue arrmakeFunctionStub(List<RightValue> args) {
        return arrmakeStub(args);
    }

    @Override
    protected RightValue ArrmakeFunctionStub(List<RightValue> args) {
        return arrmakeStub(args);
    }

    private RightValue arrmakeStub(List<RightValue> args) {
        return new ArrayValue(
            Stream.generate(() -> args.get(1)).limit(args.get(0).asInt().getValue()).toArray(RightValue[]::new)
        );
    }
}
