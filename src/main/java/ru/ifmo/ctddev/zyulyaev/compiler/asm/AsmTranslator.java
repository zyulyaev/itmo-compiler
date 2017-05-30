package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmBinary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmNullary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.instruction.AsmUnary;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmGlobl;
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
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class AsmTranslator {
    private static final AsmSymbol MALLOC = new AsmSymbol("_malloc");
    private static final AsmSymbol MEMCPY = new AsmSymbol("_memcpy");

    private final Map<BcFunction, AsmSymbol> symbolMap = new HashMap<>();
    private final Set<String> symbols = new HashSet<>();

    private final AsmOutput output = new AsmOutput();

    public List<AsmLine> translate(BcProgram program) {
        symbolMap.put(program.getMain().getFunction(), reserve("_main"));
        Stream.of(program.getFunctions().keySet(), program.getExternalFunctions().keySet())
            .flatMap(Collection::stream)
            .forEach(function -> symbolMap.put(function, reserve("_" + function.getName())));

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
        int variables = function.getDefinedVariables().size();
        Map<BcLine, AsmSymbol> lineLabels = buildLineLabels(definition);
        FunctionContext context = new FunctionContext(function, lineLabels);
        output.write(new AsmLabelLine(symbolMap.get(function)));
        output.write(AsmBinary.ENTER.create(new AsmImmediate(0), new AsmImmediate(variables * 4)));
        for (BcLine line = definition.getBody(); line != null; line = line.getNext()) {
            line.getInstruction().accept(context).forEach(output::write);
            if (lineLabels.containsKey(line)) {
                output.write(new AsmLabelLine(lineLabels.get(line)));
            }
        }
    }

    private Map<BcLine, AsmSymbol> buildLineLabels(BcFunctionDefinition definition) {
        Map<BcLine, AsmSymbol> result = new HashMap<>();
        int counter = 0;
        for (BcLine line = definition.getBody(); line != null; line = line.getNext()) {
            if (line.getInstruction() instanceof BcJump) {
                BcLine target = ((BcJump) line.getInstruction()).getAfterLine();
                result.put(target, reserve(definition.getFunction().getName() + "_" + (counter++)));
            }
        }
        return result;
    }

    private AsmSymbol reserve(String symbol) {
        if (!symbols.add(symbol)) {
            throw new IllegalStateException("Symbol already reserved: " + symbol);
        }
        return new AsmSymbol(symbol);
    }

    private class FunctionContext implements BcInstructionVisitor<Stream<AsmInstruction>> {
        private final Map<BcVariable, AsmPointer> variables = new HashMap<>();
        private final Map<BcLine, AsmSymbol> lineLabels;

        private FunctionContext(BcFunction function, Map<BcLine, AsmSymbol> lineLabels) {
            this.lineLabels = lineLabels;
            for (int i = 0; i < function.getParameters().size(); i++) {
                BcVariable variable = function.getParameters().get(i);
                variables.put(variable, new AsmPointer(AsmRegister.EBP, 4 * i + 8));
            }
            for (int i = 0; i < function.getDefinedVariables().size(); i++) {
                BcVariable variable = function.getDefinedVariables().get(i);
                variables.put(variable, new AsmPointer(AsmRegister.EBP, -4 * i));
            }
        }

        @Override
        public Stream<AsmInstruction> visit(BcArrayInit arrayInit) {
            int bytes = arrayInit.getSize() * 4;
            return Stream.of(
                AsmUnary.PUSH.create(new AsmImmediate(bytes)),
                AsmUnary.CALL.create(MALLOC),
                AsmBinary.LEA.create(AsmRegister.ECX, new AsmPointer(AsmRegister.ESP, 4)),
                AsmUnary.PUSH.create(AsmRegister.ECX),
                AsmUnary.PUSH.create(AsmRegister.EAX),
                AsmUnary.CALL.create(MEMCPY),
                AsmBinary.ADD.create(AsmRegister.ESP, new AsmImmediate(12 + bytes))
            );
        }

        @Override
        public Stream<AsmInstruction> visit(BcBinOp binOp) {
            return Operators.instructions(binOp.getOperator());
        }

        @Override
        public Stream<AsmInstruction> visit(BcJump jump) {
            AsmSymbol symbol = lineLabels.get(jump.getAfterLine());
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
    }
}
