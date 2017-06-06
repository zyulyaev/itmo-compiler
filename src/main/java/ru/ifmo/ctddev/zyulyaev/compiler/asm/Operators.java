package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmBinary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmNullary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmUnary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmImmediate;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgBinaryOperator;

import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
class Operators {
    private Operators() {
    }

    static Stream<AsmInstruction> instructions(AsgBinaryOperator operator) {
        Stream.Builder<AsmInstruction> builder = Stream.builder();
        builder.add(AsmUnary.POP.create(AsmRegister.ECX));
        builder.add(AsmUnary.POP.create(AsmRegister.EAX));
        switch (operator) {
        case ADD:
            simpleArithmetic(builder, AsmBinary.ADD);
            break;
        case SUB:
            simpleArithmetic(builder, AsmBinary.SUB);
            break;
        case MUL:
            simpleArithmetic(builder, AsmBinary.MUL);
            break;
        case DIV:
            divArithmetic(builder, AsmRegister.EAX);
            break;
        case MOD:
            divArithmetic(builder, AsmRegister.EDX);
            break;
        case LT:
            comparison(builder, AsmUnary.SETL);
            break;
        case LTE:
            comparison(builder, AsmUnary.SETLE);
            break;
        case GT:
            comparison(builder, AsmUnary.SETG);
            break;
        case GTE:
            comparison(builder, AsmUnary.SETGE);
            break;
        case EQ:
            comparison(builder, AsmUnary.SETE);
            break;
        case NEQ:
            comparison(builder, AsmUnary.SETNE);
            break;
        case OR:
            builder.add(AsmBinary.OR.create(AsmRegister.EAX, AsmRegister.ECX));
            builder.add(AsmBinary.MOV.create(AsmRegister.EAX, new AsmImmediate(0)));
            builder.add(AsmUnary.SETNE.create(AsmRegister.AL));
            builder.add(AsmUnary.PUSH.create(AsmRegister.EAX));
            break;
        case AND:
            builder.add(AsmBinary.TEST.create(AsmRegister.EAX, AsmRegister.EAX));
            builder.add(AsmBinary.MOV.create(AsmRegister.EAX, new AsmImmediate(0)));
            builder.add(AsmUnary.SETNE.create(AsmRegister.AL));
            builder.add(AsmBinary.MOV.create(AsmRegister.EDX, AsmRegister.EAX));
            builder.add(AsmBinary.TEST.create(AsmRegister.ECX, AsmRegister.ECX));
            builder.add(AsmUnary.SETNE.create(AsmRegister.AL));
            builder.add(AsmBinary.TEST.create(AsmRegister.EAX, AsmRegister.EDX));
            builder.add(AsmUnary.SETNE.create(AsmRegister.AL));
            builder.add(AsmUnary.PUSH.create(AsmRegister.EAX));
            break;
        default:
            throw new UnsupportedOperationException("Operator not supported: " + operator);
        }
        return builder.build();
    }

    private static void simpleArithmetic(Stream.Builder<AsmInstruction> builder, AsmBinary operator) {
        builder.add(operator.create(AsmRegister.EAX, AsmRegister.ECX));
        builder.add(AsmUnary.PUSH.create(AsmRegister.EAX));
    }

    private static void divArithmetic(Stream.Builder<AsmInstruction> builder, AsmRegister output) {
        builder.add(AsmNullary.CLTD);
        builder.add(AsmUnary.DIV.create(AsmRegister.ECX));
        builder.add(AsmUnary.PUSH.create(output));
    }

    private static void comparison(Stream.Builder<AsmInstruction> builder, AsmUnary setOperator) {
        builder.add(AsmBinary.CMP.create(AsmRegister.EAX, AsmRegister.ECX));
        builder.add(AsmBinary.MOV.create(AsmRegister.EAX, new AsmImmediate(0)));
        builder.add(setOperator.create(AsmRegister.AL));
        builder.add(AsmUnary.PUSH.create(AsmRegister.EAX));
    }
}
