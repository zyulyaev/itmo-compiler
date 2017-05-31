package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import org.apache.commons.lang3.SystemUtils;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmBinary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmNullary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmUnary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmGlobl;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmInstuctionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmSectionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmImmediate;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmPointer;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmSymbol;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcArrayInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcBinOp;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstructionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJump;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcNullaryInstructions;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPush;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPushAddress;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcStringInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcUnset;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcInstructionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLineVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class AsmTranslator {
    private final Set<String> symbols = new HashSet<>();
    private final Map<BcFunction, AsmSymbol> symbolMap = new HashMap<>();

    private final AsmSymbol malloc = reserveFunction("malloc");
    private final AsmSymbol memcpy = reserveFunction("memcpy");
    private final AsmSymbol arrget = reserveFunction("rc_arrget");
    private final AsmSymbol arrinit = reserveFunction("rc_arrinit");

    private final AsmOutput output = new AsmOutput();

    public List<AsmLine> translate(BcProgram program) {
        symbolMap.put(program.getMain().getFunction(), reserveFunction("main"));
        program.getExternalFunctions().keySet()
            .forEach(function -> symbolMap.put(function, reserveFunction("rc_" + function.getName())));
        program.getFunctions().keySet()
            .forEach(function -> symbolMap.put(function, reserveFunction(function.getName())));

        output.write(AsmSectionLine.TEXT);
        output.write(new AsmGlobl(symbolMap.get(program.getMain().getFunction())));
        for (BcFunctionDefinition definition : program.getFunctions().values()) {
            translateFunction(definition);
        }
        translateFunction(program.getMain());
        return output.getLines();
    }

    private void translateFunction(BcFunctionDefinition definition) {
        BcFunction function = definition.getFunction();
        int variables = definition.getLocalVariables().size();
        Map<BcLabel, AsmSymbol> lineLabels = buildLineLabels(definition);
        FunctionContext context = new FunctionContext(definition, lineLabels);
        output.write(new AsmLabelLine(symbolMap.get(function)));
        output.write(AsmBinary.ENTER.create(new AsmImmediate(0), new AsmImmediate(variables * 4)));
        for (BcLine line : definition.getBody()) {
            line.accept(context).forEach(output::write);
        }
    }

    private Map<BcLabel, AsmSymbol> buildLineLabels(BcFunctionDefinition definition) {
        Map<BcLabel, AsmSymbol> result = new HashMap<>();
        for (BcLine line : definition.getBody()) {
            line.accept(new BcLineVisitor<Void>() {
                @Override
                public Void visit(BcInstructionLine instructionLine) {
                    return null;
                }

                @Override
                public Void visit(BcLabelLine labelLine) {
                    BcLabel label = labelLine.getLabel();
                    result.put(label, reserve(definition.getFunction().getName() + "_" + label.getName()));
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

    private AsmSymbol reserveFunction(String name) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return reserve("_" + name);
        } else {
            return reserve(name);
        }
    }

    private class FunctionContext implements BcInstructionVisitor<Stream<AsmInstruction>>,
        BcLineVisitor<Stream<AsmLine>>
    {
        private final Map<BcVariable, AsmPointer> variables = new HashMap<>();
        private final Map<BcLabel, AsmSymbol> lineLabels;

        private FunctionContext(BcFunctionDefinition definition, Map<BcLabel, AsmSymbol> lineLabels) {
            this.lineLabels = lineLabels;
            BcFunction function = definition.getFunction();
            for (int i = 0; i < function.getParameters().size(); i++) {
                BcVariable variable = function.getParameters().get(i);
                variables.put(variable, new AsmPointer(AsmRegister.EBP, 4 * i + 8));
            }
            for (int i = 0; i < definition.getLocalVariables().size(); i++) {
                BcVariable variable = definition.getLocalVariables().get(i);
                variables.put(variable, new AsmPointer(AsmRegister.EBP, -4 * i - 4));
            }
        }

        @Override
        public Stream<AsmInstruction> visit(BcArrayInit arrayInit) {
            int size = arrayInit.getSize();
            AsmPointer target = variables.get(arrayInit.getTarget());
            return Stream.of(
                AsmUnary.PUSH.create(new AsmImmediate(size)),
                AsmBinary.LEA.create(AsmRegister.EAX, new AsmPointer(AsmRegister.ESP, 4)),
                AsmUnary.PUSH.create(AsmRegister.EAX),
                AsmUnary.CALL.create(arrinit),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(size * 4 + 8)),
                AsmBinary.MOV.create(target, AsmRegister.EAX)
            );
        }

        @Override
        public Stream<AsmInstruction> visit(BcStringInit stringInit) {
            String value = stringInit.getValue();
            int chars = stringInit.getValue().length();
            AsmPointer target = variables.get(stringInit.getTarget());
            return Stream.concat(
                Stream.of(
                    AsmUnary.PUSH.create(new AsmImmediate(chars + 1)),
                    AsmUnary.CALL.create(malloc),
                    AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(4)),
                    AsmBinary.MOV.create(target, AsmRegister.EAX),
                    AsmBinary.MOVB.create(new AsmPointer(AsmRegister.EAX, chars), new AsmImmediate(0))
                ),
                IntStream.range(0, chars)
                    .mapToObj(i -> AsmBinary.MOVB.create(new AsmPointer(AsmRegister.EAX, i),
                        new AsmImmediate(value.charAt(i))))
            );
        }

        @Override
        public Stream<AsmInstruction> visit(BcBinOp binOp) {
            return Operators.instructions(binOp.getOperator());
        }

        @Override
        public Stream<AsmInstruction> visit(BcJump jump) {
            AsmSymbol symbol = lineLabels.get(jump.getLabel());
            switch (jump.getCondition()) {
            case ALWAYS: return Stream.of(AsmUnary.JMP.create(symbol));
            case IF_NOT_ZERO:
                return Stream.of(
                    AsmUnary.POP.create(AsmRegister.EAX),
                    AsmBinary.TEST.create(AsmRegister.EAX, AsmRegister.EAX),
                    AsmUnary.JNZ.create(symbol)
                );
            case IF_ZERO:
                return Stream.of(
                    AsmUnary.POP.create(AsmRegister.EAX),
                    AsmBinary.TEST.create(AsmRegister.EAX, AsmRegister.EAX),
                    AsmUnary.JZ.create(symbol)
                );
            }
            throw new UnsupportedOperationException("Jump condition not supported: " + jump.getCondition());
        }

        @Override
        public Stream<AsmInstruction> visit(BcPush push) {
            return Stream.of(AsmUnary.PUSH.create(new AsmImmediate(push.getValue())));
        }

        @Override
        public Stream<AsmInstruction> visit(BcCall call) {
            int paramsCount = call.getTarget().getParameters().size();
            return Stream.of(
                AsmUnary.CALL.create(symbolMap.get(call.getTarget())),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(paramsCount * 4)),
                AsmUnary.PUSH.create(AsmRegister.EAX)
            );
        }

        @Override
        public Stream<AsmInstruction> visit(BcPushAddress pushAddress) {
            return Stream.of(
                AsmBinary.LEA.create(AsmRegister.EAX, variables.get(pushAddress.getTarget())),
                AsmUnary.PUSH.create(AsmRegister.EAX)
            );
        }

        @Override
        public Stream<AsmInstruction> visitNop(BcNullaryInstructions nop) {
            return Stream.of(AsmNullary.NOP);
        }

        @Override
        public Stream<AsmInstruction> visitPop(BcNullaryInstructions pop) {
            return Stream.of(AsmUnary.POP.create(AsmRegister.EAX));
        }

        @Override
        public Stream<AsmInstruction> visitLoad(BcNullaryInstructions load) {
            return Stream.of(
                AsmUnary.POP.create(AsmRegister.EAX),
                AsmUnary.PUSH.create(new AsmPointer(AsmRegister.EAX, 0))
            );
        }

        @Override
        public Stream<AsmInstruction> visitStore(BcNullaryInstructions store) {
            return Stream.of(
                AsmUnary.POP.create(AsmRegister.EDX),
                AsmUnary.POP.create(AsmRegister.EAX),
                AsmBinary.MOV.create(new AsmPointer(AsmRegister.EAX, 0), AsmRegister.EDX)
            );
        }

        @Override
        public Stream<AsmInstruction> visitReturn(BcNullaryInstructions ret) {
            return Stream.of(
                AsmUnary.POP.create(AsmRegister.EAX),
                AsmNullary.LEAVE,
                AsmNullary.RET
            );
        }

        @Override
        public Stream<AsmInstruction> visit(BcUnset unset) {
            // TODO decrement reference counter
            return Stream.empty();
        }

        @Override
        public Stream<AsmInstruction> visitIndex(BcNullaryInstructions index) {
            return Stream.of(
                AsmUnary.POP.create(AsmRegister.ECX),
                AsmUnary.POP.create(AsmRegister.EAX),
                // todo prettify
                AsmUnary.PUSH.create(AsmRegister.ECX),
                AsmUnary.PUSH.create(AsmRegister.EAX),
                AsmUnary.CALL.create(arrget),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(8)),
                AsmUnary.PUSH.create(AsmRegister.EAX)
            );
        }

        @Override
        public Stream<AsmLine> visit(BcInstructionLine instructionLine) {
            return instructionLine.getInstruction().accept(this).map(AsmInstuctionLine::new);
        }

        @Override
        public Stream<AsmLine> visit(BcLabelLine labelLine) {
            return Stream.of(new AsmLabelLine(lineLabels.get(labelLine.getLabel())));
        }
    }
}
