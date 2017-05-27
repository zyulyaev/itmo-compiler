package ru.ifmo.ctddev.zyulyaev.compiler;

import com.google.common.collect.ImmutableMap;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.ExternalFunction;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class Runtime {
    private final Scanner inputScanner = new Scanner(System.in);
    private final ExternalFunction readFunction = new ExternalFunction("read", 0);
    private final ExternalFunction writeFunction = new ExternalFunction("write", 1);

    private final Map<ExternalFunction, Function<List<Value>, Value>> functionDefinitions =
        ImmutableMap.<ExternalFunction, Function<List<Value>, Value>>builder()
            .put(readFunction, args -> {
                System.out.print("> ");
                return new IntValue(inputScanner.nextInt());
            })
            .put(writeFunction, args -> {
                System.out.println(args.get(0).stringValue());
                return null;
            })
            .build();


    public Map<ExternalFunction, Function<List<Value>, Value>> getFunctionDefinitions() {
        return functionDefinitions;
    }

    public Collection<ExternalFunction> getExternalFunctions() {
        return Arrays.asList(readFunction, writeFunction);
    }
}
