package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import lombok.NonNull;
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
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcInstructionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLineVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcImmediateValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcNoneValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValueVisitor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static ru.ifmo.ctddev.zyulyaev.compiler.asm.Environment.sizeOf;

/**
 * @author zyulyaev
 * @since 08.06.2017
 */
class FunctionTranslator {
    private final Environment env;
    private final AsmBuilder output;
    private final GarbageCollector gc;

    FunctionTranslator(Environment env, AsmBuilder output, GarbageCollector gc) {
        this.env = env;
        this.output = output;
        this.gc = gc;
    }

    void translate(AsmSymbol symbol, AsgVariable thisValue, List<AsgVariable> parameters,
        List<AsgVariable> localVariables, List<BcLine> body)
    {
        Map<BcLabel, AsmSymbol> lineLabels = buildLineLabels(body, symbol);
        AsmBuilder tempOutput = new AsmBuilder();
        AsmSymbol cleanup = env.reserveLabel("cleanup", symbol);
        FunctionContext ctx = new FunctionContext(thisValue, parameters, localVariables, lineLabels, cleanup,
            FunctionContext.REGISTER_STACK.size());
        LineTranslator translator = new LineTranslator(tempOutput, env, ctx, gc);
        body.forEach(line -> line.accept(translator));

        output.write(symbol);
        FunctionContext.REGISTER_STACK.forEach(reg -> output.write(AsmUnary.PUSH, reg));
        output.write(AsmBinary.ENTER, new AsmImmediate(0), new AsmImmediate(ctx.getStackSize()));
        if (ctx.getStackSize() != 0) {
            output
                .write(AsmBinary.MOV, AsmRegister.EAX, AsmRegister.ESP)
                .write(AsmUnary.PUSH, new AsmImmediate(ctx.getStackSize()))
                .write(AsmUnary.PUSH, new AsmImmediate(0))
                .write(AsmUnary.PUSH, AsmRegister.EAX)
                .write(AsmUnary.CALL, env.memset)
                .write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(12));
        }
        output.append(tempOutput);
        output
            .write(cleanup)
            .write(AsmUnary.PUSH, AsmRegister.EAX)
            .write(AsmUnary.PUSH, AsmRegister.EDX);
        Stream.of(
            localVariables.stream(),
            parameters.stream(),
            thisValue == null ? Stream.<AsgVariable>empty() : Stream.of(thisValue)
        ).flatMap(Function.identity()).forEach(variable -> {
            AsmPointer pointer = ctx.getVariablePointer(variable);
            VirtualRegisterValue value = VirtualRegisterValue.of(variable.getType(), pointer, pointer.shift(4));
            gc.decrementRc(output, value);
        });
        output
            .write(AsmUnary.POP, AsmRegister.EDX)
            .write(AsmUnary.POP, AsmRegister.EAX)
            .write(AsmNullary.LEAVE);
        for (int i = FunctionContext.REGISTER_STACK.size() - 1; i >= 0; i--) {
            output.write(AsmUnary.POP, FunctionContext.REGISTER_STACK.get(i));
        }
        output.write(AsmNullary.RET);
    }

    void buildDestructor(AsgDataType dataType) {
        AsmSymbol destructorSymbol = env.getDestructorSymbol(dataType);
        output.write(destructorSymbol);
        DataLayout layout = env.getDataLayout(dataType);
        AsmPointer thisPointer = new AsmPointer(AsmRegister.ESP, 4);
        for (AsgDataType.Field field : dataType.getFields()) {
            if (!field.getType().isPrimitive()) {
                // store this
                output.write(AsmBinary.MOV, AsmRegister.EDX, thisPointer);
                AsmPointer pointer = new AsmPointer(AsmRegister.EDX, Header.Data.SIZE + layout.getFieldOffset(field));
                gc.decrementRc(output, VirtualRegisterValue.of(field.getType(), pointer, pointer.shift(4)));
            }
        }
        output
            .write(AsmUnary.PUSH, thisPointer)
            .write(AsmUnary.CALL, env.free)
            .write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(4));
        output.write(AsmNullary.RET);
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
                    result.put(labelLine.getLabel(), env.reserveLabel(labelLine.getLabel().getName(), parent));
                    return null;
                }
            });
        }
        return result;
    }

    private static class LineTranslator implements BcLineVisitor<Void>, BcInstructionVisitor<VirtualRegisterValue>,
        BcValueVisitor<VirtualRegisterValue>
    {
        private final Map<BcRegister, VirtualRegisterValue> virtualRegisterValues = new HashMap<>();
        private final AsmBuilder builder;
        private final Environment env;
        private final FunctionContext ctx;
        private final GarbageCollector gc;

        LineTranslator(AsmBuilder builder, Environment env, FunctionContext ctx, GarbageCollector gc) {
            this.builder = builder;
            this.env = env;
            this.ctx = ctx;
            this.gc = gc;
        }

        /**
         * Uses EAX register
         */
        private void storeToMemory(AsgType type, AsmPointer target, VirtualRegisterValue value, boolean destruct) {
            if (destruct) {
                gc.decrementRc(builder, VirtualRegisterValue.of(type, target, target.shift(4)));
            }
            builder.move(target, value.getMain(), AsmRegister.EAX);
            if (value.hasAux()) {
                builder.move(target.shift(4), value.getAux(), AsmRegister.EAX);
            }
        }

        /**
         * Loads to EAX and EDX(if class) registers
         */
        private VirtualRegisterValue allocateFromMemory(AsgType type, AsmPointer source) {
            VirtualRegisterValue result = allocate(type, source, source.shift(4));
            gc.incrementRc(builder, result, new AsmImmediate(1));
            return result;
        }

        /**
         * Potentially uses EAX, thus don't use EAX for aux
         */
        private VirtualRegisterValue allocate(@NonNull AsgType type, @NonNull AsmOperand main,
            @Nullable AsmOperand aux)
        {
            VirtualRegisterValue allocated = type.isClass()
                ? new VirtualRegisterValue(type, ctx.allocate(), ctx.allocate())
                : new VirtualRegisterValue(type, ctx.allocate());
            builder.move(allocated.getMain(), main, AsmRegister.EAX);
            if (allocated.hasAux()) {
                Objects.requireNonNull(aux, "aux");
                builder.move(allocated.getAux(), aux, AsmRegister.EAX);
            }
            return allocated;
        }

        private void assign(@NonNull BcRegister register, @NonNull VirtualRegisterValue value) {
            boolean expected = register.getType().isClass();
            boolean actual = value.hasAux();
            if (expected ^ actual) {
                throw new IllegalArgumentException("Trying to store fat pointer to plain value or vice versa");
            }
            virtualRegisterValues.put(register, value);
        }

        /**
         * Pools memory and frees virtual register values map
         */
        private void deallocate(@NonNull BcValue... values) {
            for (BcValue value : values) {
                if (value instanceof BcRegister) {
                    BcRegister register = (BcRegister) value;
                    VirtualRegisterValue registerValue = virtualRegisterValues.get(register);
                    ctx.deallocate(registerValue.getMain());
                    if (registerValue.hasAux()) {
                        ctx.deallocate(registerValue.getAux());
                    }
                    virtualRegisterValues.remove(register);
                }
            }
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
                assign(destination, value);
            } else if (value != null) {
                gc.decrementRc(builder, value);
            }
            return null;
        }

        @Override
        public Void visit(BcLabelLine labelLine) {
            builder.write(ctx.getLabel(labelLine.getLabel()));
            return null;
        }

        private void pushReverse(List<BcValue> values) {
            for (int i = values.size() - 1; i >= 0; i--) {
                BcValue value = values.get(i);
                VirtualRegisterValue virtualValue = value.accept(this);
                if (virtualValue.hasAux()) {
                    builder.write(AsmUnary.PUSH, virtualValue.getAux());
                }
                builder.write(AsmUnary.PUSH, virtualValue.getMain());
                deallocate(value);
            }
        }

        @Override
        public VirtualRegisterValue visit(BcArrayInit arrayInit) {
            int length = arrayInit.getValues().size();
            int size = sizeOf(arrayInit.getArrayType().getCompound());
            pushReverse(arrayInit.getValues());
            builder
                .write(AsmUnary.PUSH, new AsmImmediate(size))
                .write(AsmUnary.PUSH, new AsmImmediate(length))
                .write(AsmBinary.LEA, AsmRegister.EAX, new AsmPointer(AsmRegister.ESP, 8))
                .write(AsmUnary.PUSH, AsmRegister.EAX)
                .write(AsmUnary.CALL, env.arrinit)
                .write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(length * size + 12));
            return allocate(arrayInit.getArrayType(), AsmRegister.EAX, null);
        }

        @Override
        public VirtualRegisterValue visit(BcStringInit stringInit) {
            String value = stringInit.getValue();
            AsmSymbol stringSymbol = env.getStringSymbol(value);
            builder
                .write(AsmUnary.PUSH, stringSymbol.toAddress())
                .write(AsmUnary.PUSH, new AsmImmediate(value.length()))
                .write(AsmUnary.CALL, env.strinit)
                .write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(8));
            return allocate(AsgPredefinedType.STRING, AsmRegister.EAX, null);
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
                AsmRegister leftRegister = builder.loadAsRegister(left, AsmRegister.EAX);
                AsmRegister rightRegister = builder.loadAsRegister(right, AsmRegister.ECX);
                builder.write(AsmBinary.LEA, AsmRegister.EAX, new AsmPointer(leftRegister, 1, rightRegister, 0));
                return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
            }
            case SUB:
                builder
                    .write(AsmBinary.MOV, AsmRegister.EAX, left)
                    .write(AsmBinary.SUB, AsmRegister.EAX, right);
                return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
            case MUL:
                builder
                    .write(AsmBinary.MOV, AsmRegister.EAX, left)
                    .write(AsmBinary.IMUL, AsmRegister.EAX, right);
                return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
            case DIV:
            case MOD: {
                AsmRegister divisor = builder.loadAsRegister(right, AsmRegister.ECX);
                builder
                    .write(AsmBinary.MOV, AsmRegister.EAX, left)
                    .write(AsmNullary.CLTD)
                    .write(AsmUnary.DIV, divisor);
                return allocate(
                    AsgPredefinedType.INT,
                    operator == AsgBinaryOperator.DIV ? AsmRegister.EAX : AsmRegister.EDX,
                    null
                );
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
                AsmRegister leftRegister = builder.loadAsRegister(left, AsmRegister.ECX);
                builder
                    .write(AsmBinary.XOR, AsmRegister.EAX, AsmRegister.EAX)
                    .write(AsmBinary.OR, leftRegister, right)
                    .write(AsmUnary.SETNE, AsmRegister.AL);
                return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
            }
            case AND: {
                builder
                    .write(AsmBinary.XOR, AsmRegister.EAX, AsmRegister.EAX)
                    .write(AsmBinary.XOR, AsmRegister.ECX, AsmRegister.ECX);
                AsmRegister leftRegister = builder.loadAsRegister(left, AsmRegister.EDX);
                builder
                    .write(AsmBinary.TEST, leftRegister, leftRegister)
                    .write(AsmUnary.SETNE, AsmRegister.AL);
                AsmRegister rightRegister = builder.loadAsRegister(right, AsmRegister.EDX);
                builder
                    .write(AsmBinary.TEST, rightRegister, rightRegister)
                    .write(AsmUnary.SETNE, AsmRegister.CL)
                    .write(AsmBinary.AND, AsmRegister.EAX, AsmRegister.ECX);
                return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
            }
            }
            throw new UnsupportedOperationException("Operator not supported: " + operator);
        }

        private VirtualRegisterValue comparison(AsmUnary setOperator, AsmOperand left, AsmOperand right) {
            AsmRegister leftRegister = builder.loadAsRegister(left, AsmRegister.ECX);
            builder
                .write(AsmBinary.XOR, AsmRegister.EAX, AsmRegister.EAX)
                .write(AsmBinary.CMP, leftRegister, right)
                .write(setOperator, AsmRegister.AL);
            return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
        }

        @Override
        public VirtualRegisterValue visit(BcJumpIfZero jump) {
            AsmOperand condition = jump.getCondition().accept(this).getMain();
            deallocate(jump.getCondition());
            assertNoVirtualRegisters();

            AsmSymbol symbol = ctx.getLabel(jump.getLabel());
            AsmRegister conditionRegister = builder.loadAsRegister(condition, AsmRegister.EAX);
            builder
                .write(AsmBinary.TEST, conditionRegister, conditionRegister)
                .write(AsmUnary.JZ, symbol);
            
            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcJump jump) {
            assertNoVirtualRegisters();
            builder.write(AsmUnary.JMP, ctx.getLabel(jump.getLabel()));
            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcCall call) {
            VirtualRegisterValue inlinedResult = inlineFunction(call);
            if (inlinedResult != null) {
                return inlinedResult;
            }

            AsgFunction function = call.getFunction();
            int paramsSize = function.getParameterTypes().stream()
                .mapToInt(Environment::sizeOf)
                .sum();
            pushReverse(call.getArguments());
            builder
                .write(AsmUnary.CALL, env.getFunctionSymbol(function))
                .write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(paramsSize));
            return allocate(function.getReturnType(), AsmRegister.EAX, AsmRegister.EDX);
        }

        private VirtualRegisterValue inlineFunction(BcCall call) {
            switch (call.getFunction().getName()) {
            case "arrlen":
                return inlineArrlen(call);
            case "arrmake":
            case "Arrmake":
                return inlineArrmake(call);
            }
            return null;
        }

        private VirtualRegisterValue inlineArrlen(BcCall call) {
            BcValue argument = call.getArguments().get(0);
            VirtualRegisterValue array = argument.accept(this);
            AsmRegister base = builder.loadAsRegister(array.getMain(), AsmRegister.EAX);
            VirtualRegisterValue result =
                allocate(AsgPredefinedType.INT, new AsmPointer(base, Header.Array.LENGTH_OFFSET), null);
            gc.decrementRc(builder, array);
            deallocate(argument);
            return result;
        }

        private VirtualRegisterValue inlineArrmake(BcCall call) {
            BcValue value = call.getArguments().get(1);
            pushReverse(call.getArguments());
            int paramsSize;
            if (value.getType().isClass()) {
                builder.write(AsmUnary.CALL, env.carrmake);
                paramsSize = 12;
            } else {
                builder.write(AsmUnary.CALL, env.arrmake);
                paramsSize = 8;
            }
            VirtualRegisterValue result = allocate(call.getResultType(), AsmRegister.EAX, null);
            if (!value.getType().isPrimitive()) {
                builder
                    .write(AsmBinary.MOV, AsmRegister.EAX, new AsmPointer(AsmRegister.ESP, 0))
                    .write(AsmBinary.MOV, AsmRegister.EDX, new AsmPointer(AsmRegister.ESP, 4));
                if (value.getType().isClass()) {
                    builder.write(AsmBinary.MOV, AsmRegister.ECX, new AsmPointer(AsmRegister.ESP, 8));
                }
                VirtualRegisterValue elem = VirtualRegisterValue.of(value.getType(), AsmRegister.EDX, AsmRegister.ECX);
                gc.incrementRc(builder, elem, AsmRegister.EAX);
                gc.decrementRc(builder, elem);
            }
            builder.write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(paramsSize));
            return result;
        }

        @Override
        public VirtualRegisterValue visit(BcStore store) {
            AsgVariable variable = store.getVariable();
            VirtualRegisterValue value = store.getValue().accept(this);
            storeToMemory(variable.getType(), ctx.getVariablePointer(variable), value, true);
            deallocate(store.getValue());
            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcLoad load) {
            AsgVariable variable = load.getVariable();
            return allocateFromMemory(variable.getType(), ctx.getVariablePointer(variable));
        }

        private AsmPointer buildArrayIndexPointer(AsgArrayType arrayType, AsmOperand array, AsmOperand index) {
            AsmRegister base = builder.loadAsRegister(array, AsmRegister.EDX);
            int elementSize = sizeOf(arrayType.getCompound());
            AsmRegister spread;
            int offset = Header.Array.SIZE;

            if (index.isImmediate()) {
                offset += ((AsmImmediate) index).getValue() * elementSize;
                spread = null;
            } else {
                spread = builder.loadAsRegister(index, AsmRegister.ECX);
            }
            return new AsmPointer(base, elementSize, spread, offset);
        }

        @Override
        public VirtualRegisterValue visit(BcIndexLoad indexLoad) {
            AsgArrayType arrayType = (AsgArrayType) indexLoad.getArray().getType();

            VirtualRegisterValue array = indexLoad.getArray().accept(this);
            VirtualRegisterValue index = indexLoad.getIndex().accept(this);

            AsmPointer source = buildArrayIndexPointer(arrayType, array.getMain(), index.getMain());
            VirtualRegisterValue result = allocateFromMemory(arrayType.getCompound(), source);
            gc.decrementRc(builder, array);
            deallocate(indexLoad.getArray(), indexLoad.getIndex());

            return result;
        }

        @Override
        public VirtualRegisterValue visit(BcIndexStore indexStore) {
            AsgArrayType arrayType = (AsgArrayType) indexStore.getArray().getType();

            VirtualRegisterValue array = indexStore.getArray().accept(this);
            VirtualRegisterValue index = indexStore.getIndex().accept(this);
            VirtualRegisterValue value = indexStore.getValue().accept(this);

            AsmPointer target = buildArrayIndexPointer(arrayType, array.getMain(), index.getMain());
            storeToMemory(arrayType.getCompound(), target, value, true);
            gc.decrementRc(builder, array);
            deallocate(indexStore.getArray(), indexStore.getIndex(), indexStore.getValue());

            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcDataInit dataInit) {
            DataLayout layout = env.getDataLayout(dataInit.getType());
            AsmPointer header = new AsmPointer(AsmRegister.EDX, 0);
            AsmPointer data = header.shift(Header.Data.SIZE);
            builder
                .write(AsmUnary.PUSH, new AsmImmediate(Header.Data.SIZE + layout.getSize()))
                .write(AsmUnary.CALL, env.malloc)
                .write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(4))
                .write(AsmBinary.MOV, AsmRegister.EDX, AsmRegister.EAX)
                .write(AsmBinary.MOV, header.shift(Header.COUNTER_OFFSET), new AsmImmediate(1));
            VirtualRegisterValue result = allocate(dataInit.getType(), AsmRegister.EAX, null);
            for (Map.Entry<AsgDataType.Field, BcValue> entry : dataInit.getValues().entrySet()) {
                AsgDataType.Field field = entry.getKey();
                BcValue value = entry.getValue();
                storeToMemory(field.getType(), data.shift(layout.getFieldOffset(field)), value.accept(this), false);
                deallocate(value);
            }
            return result;
        }

        @Override
        public VirtualRegisterValue visit(BcMethodCall call) {
            VirtualRegisterValue object = call.getObject().accept(this);
            AsgMethod method = call.getMethod();
            int argumentsSize = method.getParameterTypes().stream()
                .mapToInt(Environment::sizeOf)
                .sum();
            if (call.getObject().getType().isClass()) {
                // virtual call
                int methodOffset = env.getVirtualTableLayout((AsgClassType) call.getObject().getType())
                    .getMethodOffset(method);
                // push arguments
                pushReverse(call.getArguments());
                // push this
                builder.write(AsmUnary.PUSH, object.getMain());
                deallocate(call.getObject());
                // load vtable pointer
                AsmRegister vtable = builder.loadAsRegister(object.getAux(), AsmRegister.EAX);
                // call method
                builder.write(AsmUnary.CALL, new AsmPointer(vtable, methodOffset));
            } else {
                // static call
                AsgDataType dataType = (AsgDataType) call.getObject().getType();
                // push arguments
                pushReverse(call.getArguments());
                // push this
                builder.write(AsmUnary.PUSH, object.getMain());
                deallocate(call.getObject());
                // call method
                builder.write(AsmUnary.CALL, env.getMethodSymbol(dataType, method));
            }
            // clear stack
            builder.write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(argumentsSize + 4));
            return allocate(call.getResultType(), AsmRegister.EAX, AsmRegister.EDX);
        }

        @Override
        public VirtualRegisterValue visit(BcMemberLoad memberLoad) {
            AsgDataType dataType = (AsgDataType) memberLoad.getObject().getType();
            DataLayout layout = env.getDataLayout(dataType);
            int offset = Header.Data.SIZE + layout.getFieldOffset(memberLoad.getField());
            VirtualRegisterValue object = memberLoad.getObject().accept(this);

            AsmRegister base = builder.loadAsRegister(object.getMain(), AsmRegister.EAX);
            AsmPointer fieldPointer = new AsmPointer(base, offset);
            VirtualRegisterValue result = allocateFromMemory(memberLoad.getResultType(), fieldPointer);
            gc.decrementRc(builder, object);
            deallocate(memberLoad.getObject());
            return result;
        }

        @Override
        public VirtualRegisterValue visit(BcMemberStore memberStore) {
            AsgDataType dataType = (AsgDataType) memberStore.getObject().getType();
            DataLayout layout = env.getDataLayout(dataType);
            int offset = layout.getFieldOffset(memberStore.getField()) + Header.Data.SIZE;
            VirtualRegisterValue object = memberStore.getObject().accept(this);

            AsmRegister base = builder.loadAsRegister(object.getMain(), AsmRegister.EDX);
            AsmPointer fieldPointer = new AsmPointer(base, offset);

            VirtualRegisterValue value = memberStore.getValue().accept(this);
            storeToMemory(memberStore.getField().getType(), fieldPointer, value, true);

            gc.decrementRc(builder, object);
            deallocate(memberStore.getObject(), memberStore.getValue());

            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcReturn ret) {
            VirtualRegisterValue value = ret.getValue().accept(this);
            builder.write(AsmBinary.MOV, AsmRegister.EAX, value.getMain());
            if (value.hasAux()) {
                builder.write(AsmBinary.MOV, AsmRegister.EDX, value.getAux());
            }
            builder.write(AsmUnary.JMP, ctx.getCleanupLabel());
            deallocate(ret.getValue());
            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcCast cast) {
            VirtualRegisterValue value = cast.getValue().accept(this);
            AsgType fromType = cast.getValue().getType();
            AsgType targetType = cast.getTarget();
            VirtualRegisterValue result;
            if (fromType.isData() && targetType.isClass()) {
                AsmSymbol vtableSymbol = env.getVTableSymbol((AsgDataType) fromType, (AsgClassType) targetType);
                result = allocate(targetType, value.getMain(), vtableSymbol.toAddress());
            } else if (fromType.isClass() && targetType.isClass()) {
                VirtualTableLayout layout = env.getVirtualTableLayout((AsgClassType) fromType);
                int offset = layout.getSuperClassVTableOffset((AsgClassType) targetType);
                AsmRegister vtablePointer = builder.loadAsRegister(value.getAux(), AsmRegister.EDX);
                builder.write(AsmBinary.MOV, AsmRegister.EDX, new AsmPointer(vtablePointer, offset));
                result = allocate(targetType, value.getMain(), AsmRegister.EDX);
            } else if (fromType == AsgPredefinedType.NONE && !targetType.isPrimitive()) {
                result = allocate(targetType, value.getMain(), null);
            } else {
                throw new UnsupportedOperationException("Cast not supported");
            }
            deallocate(cast.getValue());
            return result;
        }

        @Override
        public VirtualRegisterValue visit(BcImmediateValue value) {
            return new VirtualRegisterValue(AsgPredefinedType.INT, new AsmImmediate(value.getValue()), null);
        }

        @Override
        public VirtualRegisterValue visit(BcNoneValue value) {
            return new VirtualRegisterValue(AsgPredefinedType.NONE, new AsmImmediate(0), null);
        }

        @Override
        public VirtualRegisterValue visit(BcRegister register) {
            return virtualRegisterValues.get(register);
        }
    }
}
