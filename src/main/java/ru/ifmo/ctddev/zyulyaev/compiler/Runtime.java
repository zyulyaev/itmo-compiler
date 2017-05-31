package ru.ifmo.ctddev.zyulyaev.compiler;

import com.google.common.collect.ImmutableMap;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.ExternalFunction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public abstract class Runtime<A, R> {
    public static final ExternalFunction readFunction = new ExternalFunction("read", 0);
    public static final ExternalFunction writeFunction = new ExternalFunction("write", 1);

    public static final ExternalFunction strlenFunction = new ExternalFunction("strlen", 1);
    public static final ExternalFunction strgetFunction = new ExternalFunction("strget", 2);
    public static final ExternalFunction strsubFunction = new ExternalFunction("strsub", 3);
    public static final ExternalFunction strsetFunction = new ExternalFunction("strset", 3);
    public static final ExternalFunction strcatFunction = new ExternalFunction("strcat", 2);
    public static final ExternalFunction strcmpFunction = new ExternalFunction("strcmp", 2);
    public static final ExternalFunction strdupFunction = new ExternalFunction("strdup", 1);
    public static final ExternalFunction strmakeFunction = new ExternalFunction("strmake", 2);

    public static final ExternalFunction arrlenFunction = new ExternalFunction("arrlen", 1);
    public static final ExternalFunction arrmakeFunction = new ExternalFunction("arrmake", 2);
    public static final ExternalFunction ArrmakeFunction = new ExternalFunction("Arrmake", 2);

    private final Map<ExternalFunction, Function<A, R>> functionDefinitions;

    protected Runtime() {
        this.functionDefinitions = ImmutableMap.<ExternalFunction, Function<A, R>>builder()
            .put(readFunction, this::readFunctionStub)
            .put(writeFunction, this::writeFunctionStub)
            .put(strlenFunction, this::strlenFunctionStub)
            .put(strgetFunction, this::strgetFunctionStub)
            .put(strsubFunction, this::strsubFunctionStub)
            .put(strsetFunction, this::strsetFunctionStub)
            .put(strcatFunction, this::strcatFunctionStub)
            .put(strcmpFunction, this::strcmpFunctionStub)
            .put(strdupFunction, this::strdupFunctionStub)
            .put(strmakeFunction, this::strmakeFunctionStub)
            .put(arrlenFunction, this::arrlenFunctionStub)
            .put(arrmakeFunction, this::arrmakeFunctionStub)
            .put(ArrmakeFunction, this::ArrmakeFunctionStub)
            .build();
    }

    public static Collection<ExternalFunction> getExternalFunctions() {
        return Arrays.asList(readFunction, writeFunction, strlenFunction, strgetFunction, strsubFunction,
            strsetFunction, strcatFunction, strcmpFunction, strdupFunction, strmakeFunction,
            arrlenFunction, arrmakeFunction, ArrmakeFunction);
    }

    protected abstract R readFunctionStub(A args);
    protected abstract R writeFunctionStub(A args);

    protected abstract R strlenFunctionStub(A args);
    protected abstract R strgetFunctionStub(A args);
    protected abstract R strsubFunctionStub(A args);
    protected abstract R strsetFunctionStub(A args);
    protected abstract R strcatFunctionStub(A args);
    protected abstract R strcmpFunctionStub(A args);
    protected abstract R strdupFunctionStub(A args);
    protected abstract R strmakeFunctionStub(A args);

    protected abstract R arrlenFunctionStub(A args);
    protected abstract R arrmakeFunctionStub(A args);
    protected abstract R ArrmakeFunctionStub(A args);

    public Map<ExternalFunction, Function<A, R>> getFunctionDefinitions() {
        return functionDefinitions;
    }
}
