package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.SystemUtils;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgBinaryOperator;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgArrayType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmBinary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmNullary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmUnary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmAsciiDirective;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmGlobl;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmIntDirective;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmSectionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmImmediate;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmOperand;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmPointer;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmSymbol;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcArrayInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcBinOp;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCast;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcDataInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcIndexLoad;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcIndexStore;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstructionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJump;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJumpIfZero;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcLoad;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcMemberLoad;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcMemberStore;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcMethodCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcReturn;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcStore;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcStringInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcMethodDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcInstructionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLineVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcImmediateValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcNoneValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValueVisitor;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class AsmTranslator {
    private static final int HEADER_COUNTER_OFFSET = 0;
    private static final List<AsmRegister> REGISTER_STACK = Arrays.asList(
        AsmRegister.EBX, AsmRegister.EDI, AsmRegister.ESI
    );

    private final Set<String> symbols = new HashSet<>();
    private final Map<AsgFunction, AsmSymbol> functionSymbolMap = new HashMap<>();
    private final Table<AsgDataType, AsgClassType, AsmSymbol> vtableSymbolTable = HashBasedTable.create();
    private final Table<AsgDataType, AsgMethod, AsmSymbol> methodSymbolTable = HashBasedTable.create();
    private final Map<AsgDataType, AsmSymbol> destructorSymbolMap = new HashMap<>();
    private final Map<String, AsmSymbol> stringSymbols = new HashMap<>();
    private final Map<AsgDataType, DataLayout> dataLayoutMap = new HashMap<>();
    private final Map<AsgClassType, VirtualTableLayout> virtualTableLayoutMap = new HashMap<>();

    private final AsmSymbol malloc = reserveFunction("malloc");
    private final AsmSymbol free = reserveFunction("free");
    private final AsmSymbol memcpy = reserveFunction("memcpy");
    private final AsmSymbol memset = reserveFunction("memset");
    private final AsmSymbol arrinit = reserveFunction("rc_arrinit");
    private final AsmSymbol strinit = reserveFunction("rc_strinit");

    private final AsmOutput output = new AsmOutput();

    private static int sizeOf(AsgType type) {
        return type.isClass() ? 8 : 4;
    }

    public List<AsmLine> translate(BcProgram program) {
        functionSymbolMap.put(program.getMain().getFunction(), reserveFunction("main"));
        program.getExternalFunctions().keySet()
            .forEach(function -> functionSymbolMap.put(function, reserveFunction("rc_" + function.getName())));
        program.getFunctions().stream()
            .map(BcFunctionDefinition::getFunction)
            .forEach(function -> functionSymbolMap.put(function, reserveFunction(function.getName())));
        program.getMethods()
            .forEach(def -> methodSymbolTable.put(def.getDataType(), def.getMethod(), reserve(
                def.getDataType().getName() + "$" +
                    def.getMethod().getParent().getName() + "$" +
                    def.getMethod().getName())));
        for (AsgDataType dataType : program.getDataDefinitions()) {
            Map<AsgDataType.Field, Integer> offsetMap = new LinkedHashMap<>();
            int size = 0;
            for (AsgDataType.Field field : dataType.getFields()) {
                offsetMap.put(field, size);
                size += sizeOf(field.getType());
            }
            dataLayoutMap.put(dataType, new DataLayout(dataType, size, offsetMap));
            destructorSymbolMap.put(dataType, reserve(dataType.getName() + "$destroy"));
        }
        for (AsgClassType classType : program.getClassDefinitions()) {
            Map<AsgMethod, Integer> offsetMap = new LinkedHashMap<>();
            int size = 0;
            for (AsgMethod method : classType.getMethods()) {
                offsetMap.put(method, size);
                size += 4;
            }
            virtualTableLayoutMap.put(classType, new VirtualTableLayout(classType, size, offsetMap));
        }

        collectStrings(program).forEach(str -> stringSymbols.put(str, reserve("str$" + stringSymbols.size())));

        output.write(AsmSectionLine.DATA);

        for (Map.Entry<String, AsmSymbol> entry : stringSymbols.entrySet()) {
            output.write(new AsmLabelLine(entry.getValue()));
            output.write(new AsmAsciiDirective(entry.getKey()));
        }

        output.write(AsmSectionLine.TEXT);
        for (AsgDataType dataType : program.getDataDefinitions()) {
            for (AsgClassType classType : dataType.getImplementedClasses()) {
                AsmSymbol symbol = reserve("vtable$$" + dataType.getName() + "$" + classType.getName());
                vtableSymbolTable.put(dataType, classType, symbol);
                output.write(new AsmLabelLine(symbol));
                for (AsgMethod method : classType.getMethods()) {
                    AsmSymbol methodSymbol = methodSymbolTable.get(dataType, method);
                    output.write(new AsmIntDirective(methodSymbol));
                }
                output.write(new AsmIntDirective(destructorSymbolMap.get(dataType)));
            }
        }
        output.write(new AsmGlobl(functionSymbolMap.get(program.getMain().getFunction())));
        program.getFunctions().forEach(this::translateFunction);
        program.getMethods().forEach(this::translateMethod);
        program.getDataDefinitions().forEach(this::buildDestructor);
        translateFunction(program.getMain());
        return output.getLines();
    }

    private Set<String> collectStrings(BcProgram program) {
        StringsCollector collector = new StringsCollector();
        program.getMethods().stream()
            .flatMap(method -> method.getBody().stream())
            .forEach(line -> line.accept(collector));
        program.getFunctions().stream()
            .flatMap(function -> function.getBody().stream())
            .forEach(line -> line.accept(collector));
        program.getMain().getBody().forEach(line -> line.accept(collector));
        return collector.getStrings();
    }

    private void translateFunction(BcFunctionDefinition definition) {
        AsgFunction function = definition.getFunction();
        AsmSymbol functionSymbol = functionSymbolMap.get(function);
        translateDefinition(functionSymbol, null, definition.getParameters(), definition.getLocalVariables(),
            definition.getBody());
    }

    private void translateMethod(BcMethodDefinition definition) {
        AsgDataType dataType = definition.getDataType();
        AsgMethod method = definition.getMethod();
        AsmSymbol methodSymbol = methodSymbolTable.get(dataType, method);
        translateDefinition(methodSymbol, definition.getThisValue(), definition.getParameters(),
            definition.getLocalVariables(), definition.getBody());
    }

    private void translateDefinition(AsmSymbol symbol, AsgVariable thisValue, List<AsgVariable> parameters,
        List<AsgVariable> localVariables, List<BcLine> body)
    {
        Map<BcLabel, AsmSymbol> lineLabels = buildLineLabels(body, symbol);
        AsmOutput tempOutput = new AsmOutput();
        AsmSymbol cleanup = reserveLabel("cleanup", symbol);
        FunctionContext context = new FunctionContext(thisValue, parameters, localVariables, lineLabels, tempOutput,
            cleanup);
        for (BcLine line : body) {
            line.accept(context);
        }
        output.write(symbol);
        REGISTER_STACK.forEach(reg -> output.write(AsmUnary.PUSH.create(reg)));
        output.write(
            AsmBinary.ENTER.create(new AsmImmediate(0), new AsmImmediate(context.getStackSize())),
            AsmBinary.MOV.create(AsmRegister.EAX, AsmRegister.ESP),
            AsmUnary.PUSH.create(new AsmImmediate(context.getStackSize())),
            AsmUnary.PUSH.create(new AsmImmediate(0)),
            AsmUnary.PUSH.create(AsmRegister.EAX),
            AsmUnary.CALL.create(memset),
            AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(12))
        );
        output.append(tempOutput);
        output.write(cleanup);
        output.write(AsmNullary.LEAVE);
        for (int i = REGISTER_STACK.size() - 1; i >= 0; i--) {
            output.write(AsmUnary.POP.create(REGISTER_STACK.get(i)));
        }
        output.write(AsmNullary.RET);
    }

    private void buildDestructor(AsgDataType dataType) {
        AsmSymbol destructorSymbol = destructorSymbolMap.get(dataType);
        output.write(destructorSymbol);
        DataLayout layout = dataLayoutMap.get(dataType);
        AsmPointer thisPointer = new AsmPointer(AsmRegister.ESP, -4);
        for (AsgDataType.Field field : dataType.getFields()) {
            if (!field.getType().isPrimitive()) {
                // store this
                output.write(AsmBinary.MOV.create(AsmRegister.EDX, thisPointer));
                AsmPointer pointer = new AsmPointer(AsmRegister.EDX, DataHeader.SIZE + layout.getFieldOffset(field));
                referenceCounterDecrement(output, field.getType(),
                    VirtualRegisterValue.of(field.getType(), pointer, pointer.shift(4)));
            }
        }
        output.write(
            AsmUnary.PUSH.create(thisPointer),
            AsmUnary.CALL.create(free)
        );
        output.write(AsmNullary.RET);
    }

    private void referenceCounterDecrement(AsmOutput output, AsgType type, VirtualRegisterValue target) {
        if (!type.isPrimitive() && type != AsgPredefinedType.NONE) {
            output.write(
                AsmUnary.PUSH.create(AsmRegister.EDX),
                AsmUnary.PUSH.create(AsmRegister.ECX)
            );
            output.write(
                AsmBinary.MOV.create(AsmRegister.EDX, target.getMain()),
                AsmBinary.TEST.create(AsmRegister.EDX, AsmRegister.EDX),
                AsmUnary.JZ.create(new AsmSymbol("1f")),
                AsmUnary.DEC.create(new AsmPointer(AsmRegister.EDX, HEADER_COUNTER_OFFSET)),
                AsmUnary.JNZ.create(new AsmSymbol("1f"))
            );
            if (type.isClass()) {
                VirtualTableLayout vtableLayout = virtualTableLayoutMap.get(type);
                int destructorOffset = vtableLayout.getDestructorOffset();
                // push this
                output.write(AsmUnary.PUSH.create(AsmRegister.EDX));
                // load vtable pointer
                output.write(AsmBinary.MOV.create(AsmRegister.EDX, target.getAux()));
                // call destructor
                output.write(AsmUnary.CALL.create(new AsmPointer(AsmRegister.EDX, destructorOffset)));
                // clear stack
                output.write(AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4)));
            } else if (type.isArray()) {
                AsgArrayType arrayType = (AsgArrayType) type;
                if (!arrayType.getCompound().isPrimitive()) {
                    // todo!
                }
            } else if (type == AsgPredefinedType.STRING) {
                output.write(
                    AsmUnary.PUSH.create(AsmRegister.EDX),
                    AsmUnary.CALL.create(free),
                    AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4))
                );
            } else {
                AsgDataType dataType = (AsgDataType) type;
                AsmSymbol destructor = destructorSymbolMap.get(dataType);
                // push this
                output.write(AsmUnary.PUSH.create(AsmRegister.EDX));
                // call destructor
                output.write(AsmUnary.CALL.create(destructor));
                // clear stack
                output.write(AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4)));
            }
            output.write(new AsmSymbol("1"));
            output.write(
                AsmUnary.POP.create(AsmRegister.ECX),
                AsmUnary.POP.create(AsmRegister.EDX)
            );
        }
    }

    private Map<BcLabel, AsmSymbol> buildLineLabels(List<BcLine> body, AsmSymbol parent) {
        Map<BcLabel, AsmSymbol> result = new HashMap<>();
        for (BcLine line : body) {
            line.accept(new BcLineVisitor<Void>() {
                @Override
                public Void visit(BcInstructionLine instructionLine) {
                    return null;
                }

                @Override
                public Void visit(BcLabelLine labelLine) {
                    BcLabel label = labelLine.getLabel();
                    result.put(label, reserveLabel(label.getName(), parent));
                    return null;
                }
            });
        }
        return result;
    }

    private AsmSymbol reserve(String symbol) {
        if (!symbols.add(symbol)) {
            throw new IllegalStateException("Symbol already reserved: " + symbol);
        }
        return new AsmSymbol(symbol);
    }

    private AsmSymbol reserveLabel(String name, AsmSymbol parent) {
        return reserve(parent.getValue() + "_" + name);
    }

    private AsmSymbol reserveFunction(String name) {
        // todo rework hack
        if (SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC_OSX) {
            name = "_" + name;
        }
        if (symbols.contains(name)) {
            return new AsmSymbol(name);
        } else {
            return reserve(name);
        }
    }

    private class FunctionContext implements BcLineVisitor<Void>, BcInstructionVisitor<VirtualRegisterValue>,
        BcValueVisitor<VirtualRegisterValue>
    {
        private final Map<AsgVariable, AsmPointer> variables = new HashMap<>();
        private final Map<BcRegister, VirtualRegisterValue> virtualRegisterValues = new HashMap<>();
        private final Set<AsmRegister> usedRegisters = new HashSet<>();
        private final Queue<AsmPointer> stackPool = new ArrayDeque<>();
        private int allocatedOnStack;
        private final Map<BcLabel, AsmSymbol> lineLabels;
        private final AsmOutput output;
        private final AsmSymbol cleanupLabel;

        private FunctionContext(AsgVariable thisValue, List<AsgVariable> parameters, List<AsgVariable> localVariables,
            Map<BcLabel, AsmSymbol> lineLabels, AsmOutput output, AsmSymbol cleanupLabel)
        {
            this.lineLabels = lineLabels;
            this.output = output;
            this.cleanupLabel = cleanupLabel;

            int parametersOffset = 20;
            if (thisValue != null) {
                variables.put(thisValue, new AsmPointer(AsmRegister.EBP, parametersOffset));
                parametersOffset += 4;
            }
            for (int i = 0; i < parameters.size(); i++) {
                AsgVariable variable = parameters.get(i);
                variables.put(variable, new AsmPointer(AsmRegister.EBP, 4 * i + parametersOffset));
            }
            for (int i = 0; i < localVariables.size(); i++) {
                AsgVariable variable = localVariables.get(i);
                variables.put(variable, new AsmPointer(AsmRegister.EBP, -4 * i - 4));
            }
            allocatedOnStack = localVariables.size();
        }

        private void storeImpl(AsmPointer target, AsmOperand value) {
            if (value.isPointer()) {
                output.write(
                    AsmBinary.MOV.create(AsmRegister.EAX, value),
                    AsmBinary.MOV.create(target, AsmRegister.EAX)
                );
            } else {
                output.write(
                    AsmBinary.MOV.create(target, value)
                );
            }
        }

        /**
         * Uses EAX register
         */
        private void storeToMemory(AsgType type, AsmPointer target, VirtualRegisterValue value, boolean destruct) {
            if (destruct) {
                referenceCounterDecrement(output, type, VirtualRegisterValue.of(type, target, target.shift(4)));
            }
            storeImpl(target, value.getMain());
            if (value.hasAux()) {
                storeImpl(target.shift(4), value.getAux());
            }
        }

        /**
         * Loads to EAX and EDX registers
         */
        private VirtualRegisterValue loadFromMemory(AsmPointer source, AsgType type) {
            if (type.isClass()) {
                output.write(
                    AsmBinary.MOV.create(AsmRegister.EAX, source),
                    AsmBinary.MOV.create(AsmRegister.EDX, source.shift(4))
                );
            } else {
                output.write(
                    AsmBinary.MOV.create(AsmRegister.EAX, source)
                );
            }
            if (!type.isPrimitive()) {
                // increment reference counter
                output.write(
                    AsmUnary.INC.create(new AsmPointer(AsmRegister.EAX, HEADER_COUNTER_OFFSET))
                );
            }
            return VirtualRegisterValue.of(type, AsmRegister.EAX, AsmRegister.EDX);
        }

        /**
         * If source is already a register, returns itself. Otherwise moves value from source to specified destination
         * and returns it.
         */
        private AsmRegister loadAsRegister(AsmOperand source, AsmRegister dest) {
            if (source.isRegister()) {
                return (AsmRegister) source;
            } else {
                output.write(AsmBinary.MOV.create(dest, source));
                return dest;
            }
        }

        private AsmOperand allocateImpl() {
            for (AsmRegister target : REGISTER_STACK) {
                if (usedRegisters.contains(target)) {
                    continue;
                }
                usedRegisters.add(target);
                return target;
            }
            if (stackPool.isEmpty()) {
                stackPool.add(new AsmPointer(AsmRegister.EBP, -4 * allocatedOnStack - 4));
                allocatedOnStack++;
            }
            return stackPool.poll();
        }

        /**
         * Allocates memory for EAX and EDX (if class type).
         */
        private void allocate(@NonNull BcRegister register, @NonNull VirtualRegisterValue value) {
            boolean expected = register.getType().isClass();
            boolean actual = value.hasAux();
            if (expected ^ actual) {
                throw new IllegalArgumentException("Trying to store fat pointer to plain value or vice versa");
            }
            VirtualRegisterValue allocated;
            if (value.hasAux()) {
                allocated = new VirtualRegisterValue(allocateImpl(), allocateImpl());
                output.write(
                    AsmBinary.MOV.create(allocated.getMain(), value.getMain()),
                    AsmBinary.MOV.create(allocated.getAux(), value.getAux())
                );
            } else {
                allocated = new VirtualRegisterValue(allocateImpl());
                output.write(
                    AsmBinary.MOV.create(allocated.getMain(), value.getMain())
                );
            }
            virtualRegisterValues.put(register, allocated);
        }

        /**
         * Pools memory
         */
        private void deallocateImpl(@NonNull AsmOperand operand) {
            if (operand.isRegister()) {
                usedRegisters.remove(operand);
            } else {
                stackPool.add((AsmPointer) operand);
            }
        }

        /**
         * Pools memory and frees virtual register values map
         */
        private void deallocate(@NonNull BcRegister register) {
            VirtualRegisterValue value = virtualRegisterValues.get(register);
            deallocateImpl(value.getMain());
            if (value.hasAux()) {
                deallocateImpl(value.getAux());
            }
            virtualRegisterValues.remove(register);
        }

        /**
         * Calls dealloc if passed value is virtual register
         */
        private void deallocate(@NonNull BcValue... values) {
            for (BcValue value : values) {
                if (value instanceof BcRegister) {
                    deallocate((BcRegister) value);
                }
            }
        }

        private int getStackSize() {
            return allocatedOnStack * 4;
        }

        private void assertNoVirtualRegisters() {
            if (!virtualRegisterValues.isEmpty()) {
                throw new IllegalStateException("Expected all registries to be deallocated");
            }
        }

        @Override
        public Void visit(BcInstructionLine instructionLine) {
            BcRegister destination = instructionLine.getDestination();
            BcInstruction instruction = instructionLine.getInstruction();
            VirtualRegisterValue value = instruction.accept(this);
            if (destination != null) {
                allocate(destination, value);
            } else if (value != null) {
                referenceCounterDecrement(output, instruction.getResultType(), value);
            }
            return null;
        }

        @Override
        public Void visit(BcLabelLine labelLine) {
            output.write(lineLabels.get(labelLine.getLabel()));
            return null;
        }

        private void pushReverse(List<BcValue> values) {
            for (int i = values.size() - 1; i >= 0; i--) {
                BcValue value = values.get(i);
                VirtualRegisterValue virtualValue = value.accept(this);
                if (virtualValue.hasAux()) {
                    output.write(AsmUnary.PUSH.create(virtualValue.getAux()));
                }
                output.write(AsmUnary.PUSH.create(virtualValue.getMain()));
                deallocate(value);
            }
        }

        @Override
        public VirtualRegisterValue visit(BcArrayInit arrayInit) {
            // TODO for non-primitive types
            int size = arrayInit.getValues().size(); // * sizeOf(arrayInit.getArrayType().getCompound());
            pushReverse(arrayInit.getValues());
            output.write(
                AsmUnary.PUSH.create(new AsmImmediate(size)),
                AsmBinary.LEA.create(AsmRegister.EAX, new AsmPointer(AsmRegister.ESP, 4)),
                AsmUnary.PUSH.create(AsmRegister.EAX),
                AsmUnary.CALL.create(arrinit),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(size * 4 + 8))
            );
            return new VirtualRegisterValue(AsmRegister.EAX);
        }

        @Override
        public VirtualRegisterValue visit(BcStringInit stringInit) {
            String value = stringInit.getValue();
            AsmSymbol stringSymbol = stringSymbols.get(value);
            output.write(
                AsmUnary.PUSH.create(stringSymbol.toAddress()),
                AsmUnary.PUSH.create(new AsmImmediate(value.length())),
                AsmUnary.CALL.create(strinit),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(8))
            );
            return new VirtualRegisterValue(AsmRegister.EAX);
        }

        @Override
        public VirtualRegisterValue visit(BcBinOp binOp) {
            VirtualRegisterValue leftValue = binOp.getLeft().accept(this);
            VirtualRegisterValue rightValue = binOp.getRight().accept(this);
            AsgBinaryOperator operator = binOp.getOperator();
            AsmOperand left = leftValue.getMain();
            AsmOperand right = rightValue.getMain();
            deallocate(binOp.getLeft(), binOp.getRight());
            switch (operator) {
            case ADD: {
                AsmRegister leftRegister = loadAsRegister(left, AsmRegister.EAX);
                AsmRegister rightRegister = loadAsRegister(right, AsmRegister.ECX);
                output.write(
                    AsmBinary.LEA.create(AsmRegister.EAX, new AsmPointer(leftRegister, 1, rightRegister, 0))
                );
                return new VirtualRegisterValue(AsmRegister.EAX);
            }
            case SUB:
                output.write(
                    AsmBinary.MOV.create(AsmRegister.EAX, left),
                    AsmBinary.SUB.create(AsmRegister.EAX, right)
                );
                return new VirtualRegisterValue(AsmRegister.EAX);
            case MUL:
                output.write(
                    AsmBinary.MOV.create(AsmRegister.EAX, left),
                    AsmBinary.IMUL.create(AsmRegister.EAX, right)
                );
                return new VirtualRegisterValue(AsmRegister.EAX);
            case DIV:
            case MOD: {
                AsmRegister divisor = loadAsRegister(right, AsmRegister.ECX);
                output.write(
                    AsmBinary.MOV.create(AsmRegister.EAX, left),
                    AsmNullary.CLTD,
                    AsmUnary.DIV.create(divisor)
                );
                return new VirtualRegisterValue(operator == AsgBinaryOperator.DIV ? AsmRegister.EAX : AsmRegister.EDX);
            }
            case LT:
                return comparison(AsmUnary.SETL, left, right);
            case LTE:
                return comparison(AsmUnary.SETLE, left, right);
            case GT:
                return comparison(AsmUnary.SETG, left, right);
            case GTE:
                return comparison(AsmUnary.SETGE, left, right);
            case EQ:
                return comparison(AsmUnary.SETE, left, right);
            case NEQ:
                return comparison(AsmUnary.SETNE, left, right);
            case OR: {
                AsmRegister leftRegister = loadAsRegister(left, AsmRegister.ECX);
                output.write(
                    AsmBinary.XOR.create(AsmRegister.EAX, AsmRegister.EAX),
                    AsmBinary.OR.create(leftRegister, right),
                    AsmUnary.SETNE.create(AsmRegister.AL)
                );
                return new VirtualRegisterValue(AsmRegister.EAX);
            }
            case AND: {
                output.write(
                    AsmBinary.XOR.create(AsmRegister.EAX, AsmRegister.EAX),
                    AsmBinary.XOR.create(AsmRegister.ECX, AsmRegister.ECX)
                );
                AsmRegister leftRegister = loadAsRegister(left, AsmRegister.EDX);
                output.write(
                    AsmBinary.TEST.create(leftRegister, leftRegister),
                    AsmUnary.SETNE.create(AsmRegister.AL)
                );
                AsmRegister rightRegister = loadAsRegister(right, AsmRegister.EDX);
                output.write(
                    AsmBinary.TEST.create(rightRegister, rightRegister),
                    AsmUnary.SETNE.create(AsmRegister.CL),
                    AsmBinary.AND.create(AsmRegister.EAX, AsmRegister.ECX)
                );
                return new VirtualRegisterValue(AsmRegister.EAX);
            }
            }
            throw new UnsupportedOperationException("Operator not supported: " + operator);
        }

        private VirtualRegisterValue comparison(AsmUnary setOperator, AsmOperand left, AsmOperand right) {
            AsmRegister leftRegister = loadAsRegister(left, AsmRegister.ECX);
            output.write(
                AsmBinary.XOR.create(AsmRegister.EAX, AsmRegister.EAX),
                AsmBinary.CMP.create(leftRegister, right),
                setOperator.create(AsmRegister.AL)
            );
            return new VirtualRegisterValue(AsmRegister.EAX);
        }

        @Override
        public VirtualRegisterValue visit(BcJumpIfZero jump) {
            AsmOperand condition = jump.getCondition().accept(this).getMain();
            deallocate(jump.getCondition());
            assertNoVirtualRegisters();

            AsmSymbol symbol = lineLabels.get(jump.getLabel());
            AsmRegister conditionRegister = loadAsRegister(condition, AsmRegister.EAX);
            output.write(
                AsmBinary.TEST.create(conditionRegister, conditionRegister),
                AsmUnary.JZ.create(symbol)
            );

            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcJump jump) {
            assertNoVirtualRegisters();
            output.write(AsmUnary.JMP.create(lineLabels.get(jump.getLabel())));
            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcCall call) {
            AsgFunction function = call.getFunction();
            int paramsSize = function.getParameterTypes().stream()
                .mapToInt(AsmTranslator::sizeOf)
                .sum();
            pushReverse(call.getArguments());
            output.write(
                AsmUnary.CALL.create(functionSymbolMap.get(function)),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(paramsSize))
            );
            return VirtualRegisterValue.of(function.getReturnType(), AsmRegister.EAX, AsmRegister.EDX);
        }

        @Override
        public VirtualRegisterValue visit(BcStore store) {
            AsgVariable variable = store.getVariable();
            VirtualRegisterValue value = store.getValue().accept(this);
            storeToMemory(variable.getType(), variables.get(variable), value, true);
            deallocate(store.getValue());
            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcLoad load) {
            AsgVariable variable = load.getVariable();
            return loadFromMemory(variables.get(variable), variable.getType());
        }

        private AsmPointer buildArrayIndexPointer(BcValue arrayValue, BcValue indexValue) {
            AsgArrayType arrayType = (AsgArrayType) arrayValue.getType();

            AsmOperand array = arrayValue.accept(this).getMain();
            AsmOperand index = indexValue.accept(this).getMain();

            AsmRegister base = loadAsRegister(array, AsmRegister.EDX);
            int elementSize = sizeOf(arrayType.getCompound());
            AsmRegister spread;
            int offset = ArrayHeader.SIZE;

            if (index.isImmediate()) {
                offset += ((AsmImmediate) index).getValue() * elementSize;
                spread = null;
            } else {
                spread = loadAsRegister(index, AsmRegister.ECX);
            }
            return new AsmPointer(base, elementSize, spread, offset);
        }

        @Override
        public VirtualRegisterValue visit(BcIndexLoad indexLoad) {
            AsgArrayType arrayType = (AsgArrayType) indexLoad.getArray().getType();
            AsmPointer source = buildArrayIndexPointer(indexLoad.getArray(), indexLoad.getIndex());
            deallocate(indexLoad.getArray(), indexLoad.getIndex());
            return loadFromMemory(source, arrayType.getCompound());
        }

        @Override
        public VirtualRegisterValue visit(BcIndexStore indexStore) {
            AsmPointer target = buildArrayIndexPointer(indexStore.getArray(), indexStore.getIndex());
            VirtualRegisterValue value = indexStore.getValue().accept(this);
            storeToMemory(indexStore.getValue().getType(), target, value, true);
            deallocate(indexStore.getArray(), indexStore.getIndex(), indexStore.getValue());
            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcDataInit dataInit) {
            DataLayout layout = dataLayoutMap.get(dataInit.getType());
            AsmPointer header = new AsmPointer(AsmRegister.EDX, 0);
            AsmPointer data = header.shift(DataHeader.SIZE);
            output.write(
                AsmUnary.PUSH.create(new AsmImmediate(DataHeader.SIZE + layout.getSize())),
                AsmUnary.CALL.create(malloc),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4)),
                AsmBinary.MOV.create(AsmRegister.EDX, AsmRegister.EAX),
                AsmBinary.MOV.create(header.shift(HEADER_COUNTER_OFFSET), new AsmImmediate(1))
            );
            for (Map.Entry<AsgDataType.Field, BcValue> entry : dataInit.getValues().entrySet()) {
                AsgDataType.Field field = entry.getKey();
                BcValue value = entry.getValue();
                storeToMemory(
                    field.getType(),
                    data.shift(layout.getFieldOffset(field)),
                    value.accept(this),
                    false
                );
                deallocate(value);
            }
            return new VirtualRegisterValue(AsmRegister.EAX);
        }

        @Override
        public VirtualRegisterValue visit(BcMethodCall call) {
            VirtualRegisterValue object = call.getObject().accept(this);
            AsgMethod method = call.getMethod();
            int argumentsSize = method.getParameterTypes().stream()
                .mapToInt(AsmTranslator::sizeOf)
                .sum();
            if (call.getObject().getType().isClass()) {
                // virtual call
                int methodOffset = virtualTableLayoutMap.get(call.getObject().getType()).getMethodOffset(method);
                // push arguments
                pushReverse(call.getArguments());
                // push this
                output.write(AsmUnary.PUSH.create(object.getMain()));
                deallocate(call.getObject());
                // load vtable pointer
                AsmRegister vtable = loadAsRegister(object.getAux(), AsmRegister.EAX);
                // call method
                output.write(AsmUnary.CALL.create(new AsmPointer(vtable, methodOffset)));
            } else {
                // static call
                AsgDataType dataType = (AsgDataType) call.getObject().getType();
                // push arguments
                pushReverse(call.getArguments());
                // push this
                output.write(AsmUnary.PUSH.create(object.getMain()));
                deallocate(call.getObject());
                // call method
                output.write(AsmUnary.CALL.create(methodSymbolTable.get(dataType, method)));
            }
            // clear stack
            output.write(AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(argumentsSize + 4)));
            return VirtualRegisterValue.of(call.getResultType(), AsmRegister.EAX, AsmRegister.EDX);
        }

        @Override
        public VirtualRegisterValue visit(BcMemberLoad memberLoad) {
            AsgDataType dataType = (AsgDataType) memberLoad.getObject().getType();
            DataLayout layout = dataLayoutMap.get(dataType);
            int offset = DataHeader.SIZE + layout.getFieldOffset(memberLoad.getField());
            AsmOperand object = memberLoad.getObject().accept(this).getMain();

            AsmRegister base = loadAsRegister(object, AsmRegister.EAX);
            AsmPointer fieldPointer = new AsmPointer(base, offset);
            deallocate(memberLoad.getObject());
            return loadFromMemory(fieldPointer, memberLoad.getResultType());
        }

        @Override
        public VirtualRegisterValue visit(BcMemberStore memberStore) {
            AsgDataType dataType = (AsgDataType) memberStore.getObject().getType();
            DataLayout layout = dataLayoutMap.get(dataType);
            int offset = layout.getFieldOffset(memberStore.getField()) + DataHeader.SIZE;
            AsmOperand object = memberStore.getObject().accept(this).getMain();

            AsmRegister base = loadAsRegister(object, AsmRegister.EDX);
            AsmPointer fieldPointer = new AsmPointer(base, offset);

            VirtualRegisterValue value = memberStore.getValue().accept(this);
            storeToMemory(memberStore.getField().getType(), fieldPointer, value, true);

            deallocate(memberStore.getObject(), memberStore.getValue());
            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcReturn ret) {
            VirtualRegisterValue value = ret.getValue().accept(this);
            output.write(AsmBinary.MOV.create(AsmRegister.EAX, value.getMain()));
            if (value.hasAux()) {
                output.write(AsmBinary.MOV.create(AsmRegister.EDX, value.getAux()));
            }
            output.write(AsmUnary.JMP.create(cleanupLabel));
            deallocate(ret.getValue());
            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcCast cast) {
            VirtualRegisterValue value = cast.getValue().accept(this);
            AsgType fromType = cast.getValue().getType();
            AsgType targetType = cast.getTarget();
            if ((!fromType.isData() || !targetType.isClass()) &&
                (fromType != AsgPredefinedType.NONE || targetType.isPrimitive()))
            {
                throw new UnsupportedOperationException("Only cast from data types to class types and from None to Boxed types are supported");
            }
            deallocate(cast.getValue());
            if (fromType.isData() && targetType.isClass()) {
                AsmSymbol vtableSymbol = vtableSymbolTable.get(fromType, targetType);
                output.write(
                    AsmBinary.MOV.create(AsmRegister.EAX, value.getMain()),
                    AsmBinary.MOV.create(AsmRegister.EDX, vtableSymbol.toAddress())
                );
            } else {
                output.write(AsmBinary.XOR.create(AsmRegister.EAX, AsmRegister.EAX));
                if (targetType.isClass()) {
                    AsmBinary.XOR.create(AsmRegister.EDX, AsmRegister.EDX);
                }
            }
            return VirtualRegisterValue.of(targetType, AsmRegister.EAX, AsmRegister.EDX);
        }

        @Override
        public VirtualRegisterValue visit(BcImmediateValue value) {
            return new VirtualRegisterValue(new AsmImmediate(value.getValue()), null);
        }

        @Override
        public VirtualRegisterValue visit(BcNoneValue value) {
            return new VirtualRegisterValue(new AsmImmediate(0), null);
        }

        @Override
        public VirtualRegisterValue visit(BcRegister register) {
            return virtualRegisterValues.get(register);
        }
    }

    @Getter
    private static class VirtualRegisterValue {
        private final AsmOperand main;
        private final AsmOperand aux;

        private VirtualRegisterValue(AsmOperand main, AsmOperand aux) {
            this.main = main;
            this.aux = aux;
        }

        private VirtualRegisterValue(AsmOperand main) {
            this(main, null);
        }

        private static VirtualRegisterValue of(AsgType type, AsmOperand main, AsmOperand aux) {
            if (type.isClass()) {
                return new VirtualRegisterValue(main, aux);
            } else {
                return new VirtualRegisterValue(main);
            }
        }

        boolean hasAux() {
            return aux != null;
        }
    }

    private static class ArrayHeader {
        private static final int SIZE = 8;
        private static final int LENGTH_OFFSET = 4;
    }

    private static class DataHeader {
        private static final int SIZE = 4;
    }

    @Data
    private static class DataLayout {
        private final AsgDataType dataType;
        private final int size;
        private final Map<AsgDataType.Field, Integer> fieldOffsetMap;

        int getFieldOffset(AsgDataType.Field field) {
            return fieldOffsetMap.get(field);
        }
    }

    @Data
    private static class VirtualTableLayout {
        private final AsgClassType classType;
        private final int destructorOffset;
        private final Map<AsgMethod, Integer> methodOffsetMap;

        int getMethodOffset(AsgMethod method) {
            return methodOffsetMap.get(method);
        }
    }
}
