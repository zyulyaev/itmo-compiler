package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import com.google.common.base.Strings;
import ru.ifmo.ctddev.zyulyaev.compiler.Runtime;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.ArrayValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.StringValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;

import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class InterpreterRuntime extends Runtime<List<Value>, Value> {
    private final Scanner in;
    private final PrintWriter out;

    public InterpreterRuntime(Scanner in, PrintWriter out) {
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
        return new IntValue(args.get(0).asString().getValue().length());
    }

    @Override
    protected Value strgetFunctionStub(List<Value> args) {
        return new IntValue(args.get(0).asString().getValue().codePointAt(args.get(1).asInt().getValue()));
    }

    @Override
    protected Value strsubFunctionStub(List<Value> args) {
        int start = args.get(1).asInt().getValue();
        int end = start + args.get(2).asInt().getValue();
        return new StringValue(args.get(0).asString().getValue().substring(start, end));
    }

    @Override
    protected Value strsetFunctionStub(List<Value> args) {
        StringValue value = args.get(0).asString();
        int idx = args.get(1).asInt().getValue();
        int ch = args.get(2).asInt().getValue();
        char[] chars = value.getValue().toCharArray();
        chars[idx] = (char) ch;
        value.setValue(new String(chars));
        return null;
    }

    @Override
    protected Value strcatFunctionStub(List<Value> args) {
        return new StringValue(args.get(0).asString().getValue() + args.get(1).asString().getValue());
    }

    @Override
    protected Value strcmpFunctionStub(List<Value> args) {
        return new IntValue(args.get(0).asString().getValue().compareTo(args.get(1).asString().getValue()));
    }

    @Override
    protected Value strdupFunctionStub(List<Value> args) {
        return new StringValue(args.get(0).asString().getValue());
    }

    @Override
    protected Value strmakeFunctionStub(List<Value> args) {
        String ch = "" + (char) args.get(1).asInt().getValue();
        int count = args.get(0).asInt().getValue();
        return new StringValue(Strings.repeat(ch, count));
    }

    @Override
    protected Value arrlenFunctionStub(List<Value> args) {
        return new IntValue(args.get(0).asArray().length());
    }

    @Override
    protected Value arrmakeFunctionStub(List<Value> args) {
        return new ArrayValue(
            Stream.generate(() -> args.get(1)).limit(args.get(0).asInt().getValue()).toArray(Value[]::new)
        );
    }

    @Override
    protected Value ArrmakeFunctionStub(List<Value> args) {
        return new ArrayValue(
            Stream.generate(() -> args.get(1)).limit(args.get(0).asInt().getValue()).toArray(Value[]::new)
        );
    }
}
