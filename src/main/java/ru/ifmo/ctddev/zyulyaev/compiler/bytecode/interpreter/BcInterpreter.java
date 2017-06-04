package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

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
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcArrayPointer;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcPointer;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcScalar;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcInstructionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLineVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcInterpreter {
    private final BcProgram program;
    private final Map<AsgExternalFunction, Function<List<BcValue>, BcValue>> externalStubs;
    private final BcStack stack = new BcStack();

    public BcInterpreter(BcProgram program, Map<AsgExternalFunction, Function<List<BcValue>, BcValue>> externalStubs) {
        this.program = program;
        this.externalStubs = externalStubs;
    }

    public void interpret() {
        new Frame(program.getMain()).interpret();
    }

    private class Frame implements BcLineVisitor<Void>, BcInstructionVisitor<Void> {
        private final BcFunctionDefinition definition;
        private final List<BcLine> body;
        private final Map<BcVariable, BcPointer> variableAddress = new HashMap<>();
        private final Map<BcLabel, Integer> labelMap = new HashMap<>();
        private int currentLine;

        private Frame(BcFunctionDefinition definition) {
            this.definition = definition;
            this.body = definition.getBody();

            BcFunction function = definition.getFunction();
            List<BcVariable> parameters = function.getParameters();
            int paramCount = parameters.size();
            for (int i = 0; i < paramCount; i++) {
                variableAddress.put(parameters.get(i), stack.at(i));
            }
            for (int line = 0; line < body.size(); line++) {
                int captured = line;
                body.get(line).accept(new BcLineVisitor<Void>() {
                    @Override
                    public Void visit(BcInstructionLine instructionLine) {
                        return null;
                    }

                    @Override
                    public Void visit(BcLabelLine labelLine) {
                        labelMap.put(labelLine.getLabel(), captured);
                        return null;
                    }
                });
            }
        }

        private void interpret() {
            for (BcVariable variable : definition.getLocalVariables()) {
                variableAddress.put(variable, stack.push(null));
            }
            for (currentLine = 0; currentLine < body.size(); currentLine++) {
                body.get(currentLine).accept(this);
            }
            BcValue returnValue = stack.pop();
            stack.pop(definition.getLocalVariables().size());
            stack.push(returnValue);
        }

        @Override
        public Void visit(BcArrayInit arrayInit) {
            List<BcValue> values = stack.pop(arrayInit.getSize());
            Collections.reverse(values);
            BcPointer target = variableAddress.get(arrayInit.getTarget());
            target.set(new BcArrayPointer(values.toArray(new BcValue[0]), 0));
            return null;
        }

        @Override
        public Void visit(BcStringInit stringInit) {
            BcPointer target = variableAddress.get(stringInit.getTarget());
            BcValue[] values = stringInit.getValue().chars()
                .mapToObj(BcScalar::new)
                .toArray(BcValue[]::new);
            target.set(new BcArrayPointer(values, 0));
            return null;
        }

        @Override
        public Void visit(BcBinOp binOp) {
            BcValue right = stack.pop();
            BcValue left = stack.pop();
            stack.push(Operators.apply(left, right, binOp.getOperator()));
            return null;
        }

        @Override
        public Void visit(BcJump jump) {
            int target = labelMap.get(jump.getLabel());
            switch (jump.getCondition()) {
                case ALWAYS:
                    currentLine = target;
                    break;
                case IF_ZERO: {
                    if (stack.pop().asScalar().getValue() == 0) {
                        currentLine = target;
                    }
                    break;
                }
                case IF_NOT_ZERO: {
                    if (stack.pop().asScalar().getValue() != 0) {
                        currentLine = target;
                    }
                    break;
                }
            }
            return null;
        }

        @Override
        public Void visit(BcPush push) {
            stack.push(new BcScalar(push.getValue()));
            return null;
        }

        @Override
        public Void visit(BcPushAddress pushAddress) {
            stack.push(variableAddress.get(pushAddress.getTarget()));
            return null;
        }

        @Override
        public Void visitNop(BcNullaryInstructions nop) {
            // nop
            return null;
        }

        @Override
        public Void visitPop(BcNullaryInstructions pop) {
            stack.pop();
            return null;
        }

        @Override
        public Void visitLoad(BcNullaryInstructions load) {
            stack.push(stack.pop().asPtr().get());
            return null;
        }

        @Override
        public Void visitStore(BcNullaryInstructions store) {
            BcValue value = stack.pop();
            stack.pop().asPtr().set(value);
            return null;
        }

        @Override
        public Void visitReturn(BcNullaryInstructions ret) {
            currentLine = body.size();
            return null;
        }

        @Override
        public Void visit(BcUnset unset) {
//            variableAddress.get(unset.getTarget()).set(null);
            return null;
        }

        @Override
        public Void visitIndex(BcNullaryInstructions index) {
            int offset = stack.pop().asScalar().getValue();
            stack.push(stack.pop().asPtr().shift(offset));
            return null;
        }

        @Override
        public Void visit(BcCall call) {
            BcFunction address = call.getTarget();
            BcFunctionDefinition definition = program.getFunctions().get(address);
            if (definition != null) {
                new Frame(definition).interpret();
                BcValue returnValue = stack.pop();
                stack.pop(definition.getFunction().getParameters().size());
                stack.push(returnValue);
            } else {
                AsgExternalFunction function = program.getExternalFunctions().get(address);
                Function<List<BcValue>, BcValue> stub = externalStubs.get(function);
                List<BcValue> args = stack.pop(function.getParameterCount());
                Collections.reverse(args);
                stack.push(stub.apply(args));
            }
            return null;
        }

        @Override
        public Void visit(BcInstructionLine instructionLine) {
            return instructionLine.getInstruction().accept(this);
        }

        @Override
        public Void visit(BcLabelLine labelLine) {
            // nothing to do
            return null;
        }
    }
}
