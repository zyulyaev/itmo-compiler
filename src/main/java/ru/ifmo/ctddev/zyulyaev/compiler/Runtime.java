package ru.ifmo.ctddev.zyulyaev.compiler;

import com.google.common.collect.ImmutableMap;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgArrayType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.std.BoxedArrmakeFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.std.SimpleExternalFunction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public abstract class Runtime<A, R> {
    public static final AsgExternalFunction readFunction = new SimpleExternalFunction("read",
        AsgPredefinedType.INT);
    public static final AsgExternalFunction writeFunction = new SimpleExternalFunction("write",
        AsgPredefinedType.NONE, AsgPredefinedType.INT);

    public static final AsgExternalFunction strlenFunction = new SimpleExternalFunction("strlen",
        AsgPredefinedType.INT, AsgPredefinedType.STRING);
    public static final AsgExternalFunction strgetFunction = new SimpleExternalFunction("strget",
        AsgPredefinedType.INT, AsgPredefinedType.STRING, AsgPredefinedType.INT);
    public static final AsgExternalFunction strsubFunction = new SimpleExternalFunction("strsub",
        AsgPredefinedType.STRING, AsgPredefinedType.STRING, AsgPredefinedType.INT, AsgPredefinedType.INT);
    public static final AsgExternalFunction strsetFunction = new SimpleExternalFunction("strset",
        AsgPredefinedType.NONE, AsgPredefinedType.STRING, AsgPredefinedType.INT, AsgPredefinedType.INT);
    public static final AsgExternalFunction strcatFunction = new SimpleExternalFunction("strcat",
        AsgPredefinedType.STRING, AsgPredefinedType.STRING, AsgPredefinedType.STRING);
    public static final AsgExternalFunction strcmpFunction = new SimpleExternalFunction("strcmp",
        AsgPredefinedType.INT, AsgPredefinedType.STRING, AsgPredefinedType.STRING);
    public static final AsgExternalFunction strdupFunction = new SimpleExternalFunction("strdup",
        AsgPredefinedType.STRING, AsgPredefinedType.STRING);
    public static final AsgExternalFunction strmakeFunction = new SimpleExternalFunction("strmake",
        AsgPredefinedType.STRING, AsgPredefinedType.INT, AsgPredefinedType.INT);

    public static final AsgExternalFunction arrlenFunction = new SimpleExternalFunction("arrlen",
        AsgPredefinedType.INT, new AsgArrayType(AsgPredefinedType.ANY));
    public static final AsgExternalFunction arrmakeFunction = new SimpleExternalFunction("arrmake",
        new AsgArrayType(AsgPredefinedType.INT), AsgPredefinedType.INT, AsgPredefinedType.INT);
    public static final AsgExternalFunction ArrmakeFunction = new BoxedArrmakeFunction();

    private final Map<AsgExternalFunction, Function<A, R>> functionDefinitions;

    protected Runtime() {
        this.functionDefinitions = ImmutableMap.<AsgExternalFunction, Function<A, R>>builder()
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

    public static Collection<AsgExternalFunction> getExternalFunctions() {
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

    public Map<AsgExternalFunction, Function<A, R>> getFunctionDefinitions() {
        return functionDefinitions;
    }
}
