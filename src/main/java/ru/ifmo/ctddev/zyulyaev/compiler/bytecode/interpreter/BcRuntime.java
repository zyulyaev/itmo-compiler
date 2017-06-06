package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.Runtime;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class BcRuntime extends Runtime<List<Value>, Value> {
    private final Scanner in;
    private final PrintStream out;

    public BcRuntime(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected Value readFunctionStub(List<Value> args) {
        out.print("> ");
        return new IntValue(in.nextInt());
    }

    @Override
    protected Value writeFunctionStub(List<Value> args) {
        out.println(args.get(0).asInt().getValue());
        return null;
    }

    @Override
    protected Value strlenFunctionStub(List<Value> args) {
        return new IntValue(args.get(0).asString().length());
    }

    @Override
    protected Value strgetFunctionStub(List<Value> args) {
        return new IntValue(args.get(0).asString().get(args.get(1).asInt().getValue()));
    }

    @Override
    protected Value strsubFunctionStub(List<Value> args) {
        char[] chars = args.get(0).asString().getChars();
        int start = args.get(1).asInt().getValue();
        int length = args.get(2).asInt().getValue();
        return new StringValue(Arrays.copyOfRange(chars, start, start + length));
    }

    @Override
    protected Value strsetFunctionStub(List<Value> args) {
        args.get(0).asString().set(args.get(1).asInt().getValue(), args.get(2).asInt().getValue());
        return null;
    }

    @Override
    protected Value strcatFunctionStub(List<Value> args) {
        char[] left = args.get(0).asString().getChars();
        char[] right = args.get(1).asString().getChars();
        char[] result = new char[left.length + right.length];
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return new StringValue(result);
    }

    @Override
    protected Value strcmpFunctionStub(List<Value> args) {
        String left = new String(args.get(0).asString().getChars());
        String right = new String(args.get(1).asString().getChars());
        return new IntValue(left.compareTo(right));
    }

    @Override
    protected Value strdupFunctionStub(List<Value> args) {
        return new StringValue(args.get(0).asString().getChars().clone());
    }

    @Override
    protected Value strmakeFunctionStub(List<Value> args) {
        int size = args.get(0).asInt().getValue();
        char ch = (char) args.get(1).asInt().getValue();
        char[] result = new char[size];
        Arrays.fill(result, ch);
        return new StringValue(result);
    }

    @Override
    protected Value arrlenFunctionStub(List<Value> args) {
        return new IntValue(args.get(0).asArray().length());
    }

    @Override
    protected Value arrmakeFunctionStub(List<Value> args) {
        return arrmake(args);
    }

    @Override
    protected Value ArrmakeFunctionStub(List<Value> args) {
        return arrmake(args);
    }

    private Value arrmake(List<Value> args) {
        int size = args.get(0).asInt().getValue();
        Value value = args.get(1);
        Value[] values = new Value[size];
        Arrays.fill(values, value);
        return new ArrayValue(values);
    }
}
