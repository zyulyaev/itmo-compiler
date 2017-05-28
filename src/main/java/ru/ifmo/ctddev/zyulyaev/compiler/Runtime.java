package ru.ifmo.ctddev.zyulyaev.compiler;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.ArrayValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.StringValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.ExternalFunction;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class Runtime {
    private final Scanner inputScanner = new Scanner(System.in);

    private final ExternalFunction readFunction = new ExternalFunction("read", 0);
    private final ExternalFunction writeFunction = new ExternalFunction("write", 1);

    private final ExternalFunction strlenFunction = new ExternalFunction("strlen", 1);
    private final ExternalFunction strgetFunction = new ExternalFunction("strget", 2);
    private final ExternalFunction strsubFunction = new ExternalFunction("strsub", 3);
    private final ExternalFunction strsetFunction = new ExternalFunction("strset", 3);
    private final ExternalFunction strcatFunction = new ExternalFunction("strcat", 2);
    private final ExternalFunction strcmpFunction = new ExternalFunction("strcmp", 2);
    private final ExternalFunction strdupFunction = new ExternalFunction("strdup", 1);
    private final ExternalFunction strmakeFunction = new ExternalFunction("strmake", 2);

    private final ExternalFunction arrlenFunction = new ExternalFunction("arrlen", 1);
    private final ExternalFunction arrmakeFunction = new ExternalFunction("arrmake", 2);
    private final ExternalFunction ArrmakeFunction = new ExternalFunction("Arrmake", 2);

    private final Map<ExternalFunction, Function<List<Value>, Value>> functionDefinitions =
        ImmutableMap.<ExternalFunction, Function<List<Value>, Value>>builder()
            .put(readFunction, args -> {
                System.out.print("> ");
                return new IntValue(inputScanner.nextInt());
            })
            .put(writeFunction, args -> {
                System.out.println(args.get(0).asInt().getValue());
                return null;
            })
            .put(strlenFunction, args -> new IntValue(args.get(0).asString().getValue().length()))
            .put(strgetFunction, args ->
                new IntValue(args.get(0).asString().getValue().codePointAt(args.get(1).asInt().getValue())))
            .put(strsubFunction, args -> {
                int start = args.get(1).asInt().getValue();
                int end = start + args.get(2).asInt().getValue();
                return new StringValue(args.get(0).asString().getValue().substring(start, end));
            })
            .put(strsetFunction, args -> {
                StringValue value = args.get(0).asString();
                int idx = args.get(1).asInt().getValue();
                int ch = args.get(2).asInt().getValue();
                char[] chars = value.getValue().toCharArray();
                chars[idx] = (char) ch;
                value.setValue(new String(chars));
                return null;
            })
            .put(strcatFunction, args ->
                new StringValue(args.get(0).asString().getValue() + args.get(1).asString().getValue()))
            .put(strcmpFunction, args ->
                new IntValue(args.get(0).asString().getValue().compareTo(args.get(1).asString().getValue())))
            .put(strdupFunction, args -> new StringValue(args.get(0).asString().getValue()))
            .put(strmakeFunction, args -> {
                String ch = "" + (char) args.get(1).asInt().getValue();
                int count = args.get(0).asInt().getValue();
                return new StringValue(Strings.repeat(ch, count));
            })

            .put(arrlenFunction, args -> new IntValue(args.get(0).asArray().length()))
            .put(arrmakeFunction, args -> new ArrayValue(
                Stream.generate(() -> args.get(1)).limit(args.get(0).asInt().getValue()).toArray(Value[]::new)
            ))
            .put(ArrmakeFunction, args -> new ArrayValue(
                Stream.generate(() -> args.get(1)).limit(args.get(0).asInt().getValue()).toArray(Value[]::new)
            ))
            .build();


    public Map<ExternalFunction, Function<List<Value>, Value>> getFunctionDefinitions() {
        return functionDefinitions;
    }

    public Collection<ExternalFunction> getExternalFunctions() {
        return Arrays.asList(readFunction, writeFunction, strlenFunction, strgetFunction, strsubFunction,
            strsetFunction, strcatFunction, strcmpFunction, strdupFunction, strmakeFunction,
            arrlenFunction, arrmakeFunction, ArrmakeFunction);
    }
}
