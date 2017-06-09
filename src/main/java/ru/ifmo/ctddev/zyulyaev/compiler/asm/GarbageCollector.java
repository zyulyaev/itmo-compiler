package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgArrayType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgTypeUtils;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmBinary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmUnary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmImmediate;
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

    void incrementRc(AsmOutput output, AsgType type, AsmRegister target) {
        if (hasReferenceCounter(type)) {
            output.write(AsmUnary.INC.create(new AsmPointer(target, 0)));
        }
    }

    void decrementRc(AsmOutput output, AsgType type, VirtualRegisterValue target) {
        if (hasReferenceCounter(type)) {
            output.write(
                AsmUnary.PUSH.create(AsmRegister.EDX),
                AsmUnary.PUSH.create(AsmRegister.ECX)
            );
            output.write(
                AsmBinary.MOV.create(AsmRegister.EDX, target.getMain()),
                AsmBinary.TEST.create(AsmRegister.EDX, AsmRegister.EDX),
                AsmUnary.JZ.create(new AsmSymbol("1f")),
                AsmUnary.DEC.create(new AsmPointer(AsmRegister.EDX, Header.COUNTER_OFFSET)),
                AsmUnary.JNZ.create(new AsmSymbol("1f"))
            );
            if (type.isClass()) {
                // push this
                output.write(AsmUnary.PUSH.create(AsmRegister.EDX));
                // load vtable pointer
                output.write(AsmBinary.MOV.create(AsmRegister.EDX, target.getAux()));
                // call destructor
                output.write(AsmUnary.CALL.create(new AsmPointer(AsmRegister.EDX, Header.VirtualTable.DESTRUCTOR_OFFSET)));
                // clear stack
                output.write(AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4)));
            } else if (type.isArray()) {
                AsgArrayType arrayType = (AsgArrayType) type;
                int depth = AsgTypeUtils.getArrayTypeDepth(arrayType);
                AsgType deepestType = AsgTypeUtils.getCompoundType(arrayType, depth);
                if (deepestType.isPrimitive()) {
                    if (depth == 1) {
                        // push array pointer
                        output.write(AsmUnary.PUSH.create(AsmRegister.EDX));
                        // call free
                        output.write(AsmUnary.CALL.create(env.free));
                        // clear stack
                        output.write(AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4)));
                    } else {
                        // push destructor function(free)
                        output.write(AsmUnary.PUSH.create(env.free.toAddress()));
                        // push depth up to which to interpret values as boxed
                        output.write(AsmUnary.PUSH.create(new AsmImmediate(depth - 1)));
                        // push array pointer
                        output.write(AsmUnary.PUSH.create(AsmRegister.EDX));
                        // delete array
                        output.write(AsmUnary.CALL.create(env.arrdel));
                        // clear stack
                        output.write(AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(12)));
                    }
                } else if (deepestType.isClass()) {
                    if (depth == 1) {
                        // push array pointer
                        output.write(AsmUnary.PUSH.create(AsmRegister.EDX));
                        // delete array
                        output.write(AsmUnary.CALL.create(env.carrdel));
                        // clear stack
                        output.write(AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4)));
                    } else {
                        // push destructor function(rc_carrdel)
                        output.write(AsmUnary.PUSH.create(env.carrdel.toAddress()));
                        // push depth up to which to interpret values as boxed
                        output.write(AsmUnary.PUSH.create(new AsmImmediate(depth - 1)));
                        // push array pointer
                        output.write(AsmUnary.PUSH.create(AsmRegister.EDX));
                        // delete array
                        output.write(AsmUnary.CALL.create(env.arrdel));
                        // clear stack
                        output.write(AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(12)));
                    }
                } else if (deepestType.isData()) {
                    AsmSymbol destructorSymbol = env.getDestructorSymbol((AsgDataType) deepestType);
                    // push destructor function
                    output.write(AsmUnary.PUSH.create(destructorSymbol.toAddress()));
                    // push depth up to which to interpret values as boxed
                    output.write(AsmUnary.PUSH.create(new AsmImmediate(depth)));
                    // push array pointer
                    output.write(AsmUnary.PUSH.create(AsmRegister.EDX));
                    // delete array
                    output.write(AsmUnary.CALL.create(env.arrdel));
                    // clear stack
                    output.write(AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(12)));
                } else {
                    throw new UnsupportedOperationException();
                }
            } else if (type == AsgPredefinedType.STRING) {
                output.write(
                    AsmUnary.PUSH.create(AsmRegister.EDX),
                    AsmUnary.CALL.create(env.free),
                    AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4))
                );
            } else {
                AsgDataType dataType = (AsgDataType) type;
                AsmSymbol destructor = env.getDestructorSymbol(dataType);
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
}
