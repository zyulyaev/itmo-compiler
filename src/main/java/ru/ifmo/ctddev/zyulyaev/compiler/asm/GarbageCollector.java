package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgArrayType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgTypeUtils;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmBinary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmUnary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmImmediate;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmOperand;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmPointer;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmSymbol;

/**
 * @author zyulyaev
 * @since 09.06.2017
 */
class GarbageCollector {
    private final Environment env;

    GarbageCollector(Environment env) {
        this.env = env;
    }

    private boolean hasReferenceCounter(AsgType type) {
        return !type.isPrimitive() && type != AsgPredefinedType.NONE;
    }

    void incrementRc(AsmBuilder builder, VirtualRegisterValue target, AsmOperand count) {
        if (hasReferenceCounter(target.getType())) {
            AsmRegister base = builder.loadAsRegister(target.getMain(), AsmRegister.EAX);
            count = builder.loadAsValue(count, AsmRegister.ECX);
            builder
                .write(AsmBinary.TEST, base, base)
                .write(AsmUnary.JZ, new AsmSymbol("1f"))
                .write(AsmBinary.ADD, new AsmPointer(base, Header.COUNTER_OFFSET), count)
                .write(new AsmSymbol("1"));
        }
    }

    void decrementRc(AsmBuilder builder, VirtualRegisterValue target) {
        AsgType type = target.getType();
        if (hasReferenceCounter(type)) {
            builder
                .write(AsmUnary.PUSH, AsmRegister.EDX)
                .write(AsmUnary.PUSH, AsmRegister.ECX)
                
                .write(AsmBinary.MOV, AsmRegister.EDX, target.getMain())
                .write(AsmBinary.TEST, AsmRegister.EDX, AsmRegister.EDX)
                .write(AsmUnary.JZ, new AsmSymbol("1f"))
                .write(AsmUnary.DEC, new AsmPointer(AsmRegister.EDX, Header.COUNTER_OFFSET))
                .write(AsmUnary.JNZ, new AsmSymbol("1f"));
            if (type.isClass()) {
                // push this
                builder.write(AsmUnary.PUSH, AsmRegister.EDX);
                // load vtable pointer
                builder.write(AsmBinary.MOV, AsmRegister.EDX, target.getAux());
                // call destructor
                builder.write(AsmUnary.CALL, new AsmPointer(AsmRegister.EDX, Header.VirtualTable.DESTRUCTOR_OFFSET));
                // clear stack
                builder.write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(4));
            } else if (type.isArray()) {
                AsgArrayType arrayType = (AsgArrayType) type;
                int depth = AsgTypeUtils.getArrayTypeDepth(arrayType);
                AsgType deepestType = AsgTypeUtils.getCompoundType(arrayType, depth);
                if (deepestType.isPrimitive()) {
                    if (depth == 1) {
                        // push array pointer
                        builder.write(AsmUnary.PUSH, AsmRegister.EDX);
                        // call free
                        builder.write(AsmUnary.CALL, env.free);
                        // clear stack
                        builder.write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(4));
                    } else {
                        // push destructor function(free)
                        builder.write(AsmUnary.PUSH, env.free.toAddress());
                        // push depth up to which to interpret values as boxed
                        builder.write(AsmUnary.PUSH, new AsmImmediate(depth - 1));
                        // push array pointer
                        builder.write(AsmUnary.PUSH, AsmRegister.EDX);
                        // delete array
                        builder.write(AsmUnary.CALL, env.arrdel);
                        // clear stack
                        builder.write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(12));
                    }
                } else if (deepestType.isClass()) {
                    if (depth == 1) {
                        // push array pointer
                        builder.write(AsmUnary.PUSH, AsmRegister.EDX);
                        // delete array
                        builder.write(AsmUnary.CALL, env.carrdel);
                        // clear stack
                        builder.write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(4));
                    } else {
                        // push destructor function(rc_carrdel)
                        builder.write(AsmUnary.PUSH, env.carrdel.toAddress());
                        // push depth up to which to interpret values as boxed
                        builder.write(AsmUnary.PUSH, new AsmImmediate(depth - 1));
                        // push array pointer
                        builder.write(AsmUnary.PUSH, AsmRegister.EDX);
                        // delete array
                        builder.write(AsmUnary.CALL, env.arrdel);
                        // clear stack
                        builder.write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(12));
                    }
                } else if (deepestType.isData()) {
                    AsmSymbol destructorSymbol = env.getDestructorSymbol((AsgDataType) deepestType);
                    // push destructor function
                    builder.write(AsmUnary.PUSH, destructorSymbol.toAddress());
                    // push depth up to which to interpret values as boxed
                    builder.write(AsmUnary.PUSH, new AsmImmediate(depth));
                    // push array pointer
                    builder.write(AsmUnary.PUSH, AsmRegister.EDX);
                    // delete array
                    builder.write(AsmUnary.CALL, env.arrdel);
                    // clear stack
                    builder.write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(12));
                } else {
                    throw new UnsupportedOperationException();
                }
            } else if (type == AsgPredefinedType.STRING) {
                builder
                    .write(AsmUnary.PUSH, AsmRegister.EDX)
                    .write(AsmUnary.CALL, env.free)
                    .write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(4));
            } else {
                AsgDataType dataType = (AsgDataType) type;
                AsmSymbol destructor = env.getDestructorSymbol(dataType);
                // push this
                builder.write(AsmUnary.PUSH, AsmRegister.EDX);
                // call destructor
                builder.write(AsmUnary.CALL, destructor);
                // clear stack
                builder.write(AsmBinary.ADD, AsmRegister.ESP, new AsmImmediate(4));
            }
            builder
                .write(new AsmSymbol("1"))
                .write(AsmUnary.POP, AsmRegister.ECX)
                .write(AsmUnary.POP, AsmRegister.EDX);
        }
    }
}
