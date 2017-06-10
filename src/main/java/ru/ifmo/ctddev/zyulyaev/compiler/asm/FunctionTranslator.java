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

/**
 * @author zyulyaev
 * @since 08.06.2017
 */
class FunctionTranslator {
    private final Environment env;
    private final AsmOutput output;
    private final GarbageCollector gc;

    FunctionTranslator(Environment env, AsmOutput output, GarbageCollector gc) {
        this.env = env;
        this.output = output;
        this.gc = gc;
    }

    void translate(AsmSymbol symbol, AsgVariable thisValue, List<AsgVariable> parameters,
        List<AsgVariable> localVariables, List<BcLine> body)
    {
        Map<BcLabel, AsmSymbol> lineLabels = buildLineLabels(body, symbol);
        AsmOutput tempOutput = new AsmOutput();
        AsmSymbol cleanup = env.reserveLabel("cleanup", symbol);
        FunctionContext ctx = new FunctionContext(thisValue, parameters, localVariables, lineLabels, cleanup,
            FunctionContext.REGISTER_STACK.size());
        LineTranslator translator = new LineTranslator(tempOutput, env, ctx, gc);
        body.forEach(line -> line.accept(translator));

        output.write(symbol);
        FunctionContext.REGISTER_STACK.forEach(reg -> output.write(AsmUnary.PUSH.create(reg)));
        output.write(AsmBinary.ENTER.create(new AsmImmediate(0), new AsmImmediate(ctx.getStackSize())));
        if (ctx.getStackSize() != 0) {
            output.write(
                AsmBinary.MOV.create(AsmRegister.EAX, AsmRegister.ESP),
                AsmUnary.PUSH.create(new AsmImmediate(ctx.getStackSize())),
                AsmUnary.PUSH.create(new AsmImmediate(0)),
                AsmUnary.PUSH.create(AsmRegister.EAX),
                AsmUnary.CALL.create(env.memset),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(12))
            );
        }
        output.append(tempOutput);
        output.write(cleanup);
        output.write(
            AsmUnary.PUSH.create(AsmRegister.EAX),
            AsmUnary.PUSH.create(AsmRegister.EDX)
        );
        Stream.of(
            localVariables.stream(),
            parameters.stream(),
            thisValue == null ? Stream.<AsgVariable>empty() : Stream.of(thisValue)
        ).flatMap(Function.identity()).forEach(variable -> {
            AsmPointer pointer = ctx.getVariablePointer(variable);
            VirtualRegisterValue value = VirtualRegisterValue.of(variable.getType(), pointer, pointer.shift(4));
            gc.decrementRc(output, value);
        });
        output.write(
            AsmUnary.POP.create(AsmRegister.EDX),
            AsmUnary.POP.create(AsmRegister.EAX),
            AsmNullary.LEAVE
        );
        for (int i = FunctionContext.REGISTER_STACK.size() - 1; i >= 0; i--) {
            output.write(AsmUnary.POP.create(FunctionContext.REGISTER_STACK.get(i)));
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
                output.write(AsmBinary.MOV.create(AsmRegister.EDX, thisPointer));
                AsmPointer pointer = new AsmPointer(AsmRegister.EDX, Header.Data.SIZE + layout.getFieldOffset(field));
                gc.decrementRc(output, VirtualRegisterValue.of(field.getType(), pointer, pointer.shift(4)));
            }
        }
        output.write(
            AsmUnary.PUSH.create(thisPointer),
            AsmUnary.CALL.create(env.free),
            AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4))
        );
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
        private final AsmOutput output;
        private final Environment env;
        private final FunctionContext ctx;
        private final GarbageCollector gc;

        LineTranslator(AsmOutput output, Environment env, FunctionContext ctx, GarbageCollector gc) {
            this.output = output;
            this.env = env;
            this.ctx = ctx;
            this.gc = gc;
        }

        /**
         * Potentially uses EAX
         */
        private void move(AsmOperand dest, AsmOperand src) {
            if (dest.isPointer() && src.isPointer()) {
                output.write(AsmBinary.MOV.create(AsmRegister.EAX, src));
                src = AsmRegister.EAX;
            }
            output.write(AsmBinary.MOV.create(dest, src));
        }

        /**
         * Uses EAX register
         */
        private void storeToMemory(AsgType type, AsmPointer target, VirtualRegisterValue value, boolean destruct) {
            if (destruct) {
                gc.decrementRc(output, VirtualRegisterValue.of(type, target, target.shift(4)));
            }
            move(target, value.getMain());
            if (value.hasAux()) {
                move(target.shift(4), value.getAux());
            }
        }

        /**
         * Loads to EAX and EDX(if class) registers
         */
        private VirtualRegisterValue allocateFromMemory(AsgType type, AsmPointer source) {
            VirtualRegisterValue result = allocate(type, source, source.shift(4));
            gc.incrementRc(output, result);
            return result;
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

        /**
         * Potentially uses EAX, thus don't use EAX for aux
         */
        private VirtualRegisterValue allocate(@NonNull AsgType type, @NonNull AsmOperand main,
            @Nullable AsmOperand aux)
        {
            VirtualRegisterValue allocated = type.isClass()
                ? new VirtualRegisterValue(type, ctx.allocate(), ctx.allocate())
                : new VirtualRegisterValue(type, ctx.allocate());
            move(allocated.getMain(), main);
            if (allocated.hasAux()) {
                Objects.requireNonNull(aux, "aux");
                move(allocated.getAux(), aux);
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
                gc.decrementRc(output, value);
            }
            return null;
        }

        @Override
        public Void visit(BcLabelLine labelLine) {
            output.write(ctx.getLabel(labelLine.getLabel()));
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
                AsmUnary.CALL.create(env.arrinit),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(size * 4 + 8))
            );
            return allocate(arrayInit.getArrayType(), AsmRegister.EAX, null);
        }

        @Override
        public VirtualRegisterValue visit(BcStringInit stringInit) {
            String value = stringInit.getValue();
            AsmSymbol stringSymbol = env.getStringSymbol(value);
            output.write(
                AsmUnary.PUSH.create(stringSymbol.toAddress()),
                AsmUnary.PUSH.create(new AsmImmediate(value.length())),
                AsmUnary.CALL.create(env.strinit),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(8))
            );
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
                AsmRegister leftRegister = loadAsRegister(left, AsmRegister.EAX);
                AsmRegister rightRegister = loadAsRegister(right, AsmRegister.ECX);
                output.write(
                    AsmBinary.LEA.create(AsmRegister.EAX, new AsmPointer(leftRegister, 1, rightRegister, 0))
                );
                return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
            }
            case SUB:
                output.write(
                    AsmBinary.MOV.create(AsmRegister.EAX, left),
                    AsmBinary.SUB.create(AsmRegister.EAX, right)
                );
                return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
            case MUL:
                output.write(
                    AsmBinary.MOV.create(AsmRegister.EAX, left),
                    AsmBinary.IMUL.create(AsmRegister.EAX, right)
                );
                return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
            case DIV:
            case MOD: {
                AsmRegister divisor = loadAsRegister(right, AsmRegister.ECX);
                output.write(
                    AsmBinary.MOV.create(AsmRegister.EAX, left),
                    AsmNullary.CLTD,
                    AsmUnary.DIV.create(divisor)
                );
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
                AsmRegister leftRegister = loadAsRegister(left, AsmRegister.ECX);
                output.write(
                    AsmBinary.XOR.create(AsmRegister.EAX, AsmRegister.EAX),
                    AsmBinary.OR.create(leftRegister, right),
                    AsmUnary.SETNE.create(AsmRegister.AL)
                );
                return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
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
                return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
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
            return allocate(AsgPredefinedType.INT, AsmRegister.EAX, null);
        }

        @Override
        public VirtualRegisterValue visit(BcJumpIfZero jump) {
            AsmOperand condition = jump.getCondition().accept(this).getMain();
            deallocate(jump.getCondition());
            assertNoVirtualRegisters();

            AsmSymbol symbol = ctx.getLabel(jump.getLabel());
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
            output.write(AsmUnary.JMP.create(ctx.getLabel(jump.getLabel())));
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
            output.write(
                AsmUnary.CALL.create(env.getFunctionSymbol(function)),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(paramsSize))
            );
            return allocate(function.getReturnType(), AsmRegister.EAX, AsmRegister.EDX);
        }

        private VirtualRegisterValue inlineFunction(BcCall call) {
            switch (call.getFunction().getName()) {
            case "arrlen":
                return inlineArrlen(call);
            }
            return null;
        }

        private VirtualRegisterValue inlineArrlen(BcCall call) {
            BcValue argument = call.getArguments().get(0);
            VirtualRegisterValue array = argument.accept(this);
            AsmRegister base = loadAsRegister(array.getMain(), AsmRegister.EAX);
            VirtualRegisterValue result =
                allocate(AsgPredefinedType.INT, new AsmPointer(base, Header.Array.LENGTH_OFFSET), null);
            gc.decrementRc(output, array);
            deallocate(argument);
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
            AsmRegister base = loadAsRegister(array, AsmRegister.EDX);
            int elementSize = Environment.sizeOf(arrayType.getCompound());
            AsmRegister spread;
            int offset = Header.Array.SIZE;

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

            VirtualRegisterValue array = indexLoad.getArray().accept(this);
            VirtualRegisterValue index = indexLoad.getIndex().accept(this);

            AsmPointer source = buildArrayIndexPointer(arrayType, array.getMain(), index.getMain());
            VirtualRegisterValue result = allocateFromMemory(arrayType.getCompound(), source);
            gc.decrementRc(output, array);
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
            gc.decrementRc(output, array);
            deallocate(indexStore.getArray(), indexStore.getIndex(), indexStore.getValue());

            return null;
        }

        @Override
        public VirtualRegisterValue visit(BcDataInit dataInit) {
            DataLayout layout = env.getDataLayout(dataInit.getType());
            AsmPointer header = new AsmPointer(AsmRegister.EDX, 0);
            AsmPointer data = header.shift(Header.Data.SIZE);
            output.write(
                AsmUnary.PUSH.create(new AsmImmediate(Header.Data.SIZE + layout.getSize())),
                AsmUnary.CALL.create(env.malloc),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4)),
                AsmBinary.MOV.create(AsmRegister.EDX, AsmRegister.EAX),
                AsmBinary.MOV.create(header.shift(Header.COUNTER_OFFSET), new AsmImmediate(1))
            );
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
                output.write(AsmUnary.CALL.create(env.getMethodSymbol(dataType, method)));
            }
            // clear stack
            output.write(AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(argumentsSize + 4)));
            return allocate(call.getResultType(), AsmRegister.EAX, AsmRegister.EDX);
        }

        @Override
        public VirtualRegisterValue visit(BcMemberLoad memberLoad) {
            AsgDataType dataType = (AsgDataType) memberLoad.getObject().getType();
            DataLayout layout = env.getDataLayout(dataType);
            int offset = Header.Data.SIZE + layout.getFieldOffset(memberLoad.getField());
            VirtualRegisterValue object = memberLoad.getObject().accept(this);

            AsmRegister base = loadAsRegister(object.getMain(), AsmRegister.EAX);
            AsmPointer fieldPointer = new AsmPointer(base, offset);
            VirtualRegisterValue result = allocateFromMemory(memberLoad.getResultType(), fieldPointer);
            gc.decrementRc(output, object);
            deallocate(memberLoad.getObject());
            return result;
        }

        @Override
        public VirtualRegisterValue visit(BcMemberStore memberStore) {
            AsgDataType dataType = (AsgDataType) memberStore.getObject().getType();
            DataLayout layout = env.getDataLayout(dataType);
            int offset = layout.getFieldOffset(memberStore.getField()) + Header.Data.SIZE;
            VirtualRegisterValue object = memberStore.getObject().accept(this);

            AsmRegister base = loadAsRegister(object.getMain(), AsmRegister.EDX);
            AsmPointer fieldPointer = new AsmPointer(base, offset);

            VirtualRegisterValue value = memberStore.getValue().accept(this);
            storeToMemory(memberStore.getField().getType(), fieldPointer, value, true);

            gc.decrementRc(output, object);
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
            output.write(AsmUnary.JMP.create(ctx.getCleanupLabel()));
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

            VirtualRegisterValue result;
            if (fromType.isData() && targetType.isClass()) {
                AsmSymbol vtableSymbol = env.getVTableSymbol((AsgDataType) fromType, (AsgClassType) targetType);
                result = allocate(targetType, value.getMain(), vtableSymbol.toAddress());
            } else {
                result = allocate(targetType, value.getMain(), null);
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
