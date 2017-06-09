package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang3.SystemUtils;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmSymbol;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcMethodDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 08.06.2017
 */
class Environment {
    private final Set<String> symbols = new HashSet<>();
    private final Map<AsgFunction, AsmSymbol> functionSymbolMap = new HashMap<>();
    private final Table<AsgDataType, AsgClassType, AsmSymbol> vtableSymbolTable = HashBasedTable.create();
    private final Table<AsgDataType, AsgMethod, AsmSymbol> methodSymbolTable = HashBasedTable.create();
    private final Map<AsgDataType, AsmSymbol> destructorSymbolMap = new HashMap<>();
    private final Map<String, AsmSymbol> stringSymbols = new HashMap<>();
    private final Map<AsgDataType, DataLayout> dataLayoutMap = new HashMap<>();
    private final Map<AsgClassType, VirtualTableLayout> virtualTableLayoutMap = new HashMap<>();

    final AsmSymbol main = reserveFunction("main");
    final AsmSymbol malloc = reserveFunction("malloc");
    final AsmSymbol free = reserveFunction("free");
    final AsmSymbol memcpy = reserveFunction("memcpy");
    final AsmSymbol memset = reserveFunction("memset");
    final AsmSymbol arrinit = reserveFunction("rc_arrinit");
    final AsmSymbol strinit = reserveFunction("rc_strinit");
    final AsmSymbol arrdel = reserveFunction("rc_arrdel");
    final AsmSymbol carrdel = reserveFunction("rc_carrdel");

    Environment(List<AsgDataType> dataTypes, List<AsgClassType> classes, List<BcFunctionDefinition> functions,
        Map<AsgFunction, AsgExternalFunction> externalFunctions, List<BcMethodDefinition> methods, AsgFunction main)
    {
        functionSymbolMap.put(main, this.main);
        Map<AsgExternalFunction, AsmSymbol> extFunctionSymbolMap = externalFunctions.values().stream()
            .distinct().collect(Collectors.toMap(
                Function.identity(),
                f -> reserveFunction("rc_" + f.getName())
            ));
        externalFunctions.forEach((key, value) -> functionSymbolMap.put(key, extFunctionSymbolMap.get(value)));
        functions.stream()
            .map(BcFunctionDefinition::getFunction)
            .forEach(function -> functionSymbolMap.put(function, reserveFunction(function.getName())));
        methods.forEach(def -> methodSymbolTable.put(def.getDataType(), def.getMethod(), reserve(
            def.getDataType().getName() + "$" +
                def.getMethod().getParent().getName() + "$" +
                def.getMethod().getName())));
        for (AsgDataType dataType : dataTypes) {
            Map<AsgDataType.Field, Integer> offsetMap = new LinkedHashMap<>();
            int size = 0;
            for (AsgDataType.Field field : dataType.getFields()) {
                offsetMap.put(field, size);
                size += sizeOf(field.getType());
            }
            dataLayoutMap.put(dataType, new DataLayout(dataType, size, offsetMap));
            destructorSymbolMap.put(dataType, reserve(dataType.getName() + "$destroy"));
        }
        for (AsgClassType classType : classes) {
            Map<AsgMethod, Integer> offsetMap = new LinkedHashMap<>();
            int offset = 4; // destructor is always first
            for (AsgMethod method : classType.getMethods()) {
                offsetMap.put(method, offset);
                offset += 4;
            }
            virtualTableLayoutMap.put(classType, new VirtualTableLayout(classType, offsetMap));
        }
        for (AsgDataType dataType : dataTypes) {
            for (AsgClassType classType : dataType.getImplementedClasses()) {
                vtableSymbolTable.put(dataType, classType,
                    reserve("vtable$$" + dataType.getName() + "$" + classType.getName()));
            }
        }
    }

    static int sizeOf(AsgType type) {
        return type.isClass() ? 8 : 4;
    }

    AsmSymbol reserve(String symbol) {
        if (!symbols.add(symbol)) {
            throw new IllegalStateException("Symbol already reserved: " + symbol);
        }
        return new AsmSymbol(symbol);
    }

    AsmSymbol reserveLabel(String name, AsmSymbol parent) {
        return reserve(parent.getValue() + "_" + name);
    }

    AsmSymbol reserveFunction(String name) {
        if (SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC_OSX) {
            name = "_" + name;
        }
        return reserve(name);
    }

    void cacheString(String str) {
        stringSymbols.putIfAbsent(str, reserve("str$" + stringSymbols.size()));
    }

    AsmSymbol getStringSymbol(String str) {
        return stringSymbols.get(str);
    }

    Set<String> getCachedStrings() {
        return stringSymbols.keySet();
    }

    AsmSymbol getFunctionSymbol(AsgFunction function) {
        return functionSymbolMap.get(function);
    }

    AsmSymbol getVTableSymbol(AsgDataType dataType, AsgClassType classType) {
        return vtableSymbolTable.get(dataType, classType);
    }

    AsmSymbol getMethodSymbol(AsgDataType dataType, AsgMethod method) {
        return methodSymbolTable.get(dataType, method);
    }

    AsmSymbol getDestructorSymbol(AsgDataType dataType) {
        return destructorSymbolMap.get(dataType);
    }

    VirtualTableLayout getVirtualTableLayout(AsgClassType classType) {
        return virtualTableLayoutMap.get(classType);
    }

    DataLayout getDataLayout(AsgDataType dataType) {
        return dataLayoutMap.get(dataType);
    }
}
