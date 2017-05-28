package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcArrayInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcBinOp;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstructionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJump;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcNullaryInstructions;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPush;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPushAddress;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcArrayPtrValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcIntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.value.BcVarAddress;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.ExternalFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcInterpreter implements BcInstructionVisitor<Void> {
    private final BcProgram program;
    private final Map<ExternalFunction, Function<List<BcValue>, BcValue>> externalStubs;
    private final List<BcValue> stack = new ArrayList<>();

    private Map<BcVariable, BcVarAddress> variableAddress = new HashMap<>();
    private BcLine currentLine;

    public BcInterpreter(BcProgram program, Map<ExternalFunction, Function<List<BcValue>, BcValue>> externalStubs) {
        this.program = program;
        this.externalStubs = externalStubs;
    }

    public void interpret() {
        interpret(program.getMain(), false);
    }

    private void interpret(BcFunctionDefinition definition, boolean cleanup) {
        BcLine line = currentLine;
        Map<BcVariable, BcVarAddress> addresses = variableAddress;

        variableAddress = new HashMap<>();

        BcFunction function = definition.getFunction();
        List<BcVariable> parameters = function.getParameters();
        int paramCount = parameters.size();
        for (int i = 0; i < paramCount; i++) {
            variableAddress.put(parameters.get(i), new BcVarAddress(stack, stack.size() - paramCount + i));
        }
        for (BcVariable variable : function.getDefinedVariables()) {
            variableAddress.put(variable, new BcVarAddress(stack, stack.size()));
            push(null);
        }
        BcLine body = definition.getBody();
        for (currentLine = body; currentLine != null; currentLine = currentLine == null ? null : currentLine.getNext())
        {
            currentLine.getInstruction().accept(this);
        }
        if (cleanup) {
            BcValue returnValue = pop();
            stack.subList(stack.size() - paramCount - function.getDefinedVariables().size(), stack.size()).clear();
            push(returnValue);
        }

        currentLine = line;
        variableAddress = addresses;
    }

    @Override
    public Void visit(BcArrayInit arrayInit) {
        int size = arrayInit.getSize();
        List<BcValue> subList = stack.subList(stack.size() - size, stack.size());
        BcValue[] values = subList.toArray(new BcValue[0]);
        subList.clear();
        push(new BcArrayPtrValue(values, 0));
        return null;
    }

    @Override
    public Void visit(BcBinOp binOp) {
        BcValue right = pop();
        BcValue left = pop();
        push(Operators.apply(left, right, binOp.getOperator()));
        return null;
    }

    @Override
    public Void visit(BcJump jump) {
        switch (jump.getCondition()) {
            case ALWAYS:
                currentLine = jump.getAfterLine();
                break;
            case IF_ZERO: {
                if (pop().asInt().getValue() == 0) {
                    currentLine = jump.getAfterLine();
                }
                break;
            }
            case IF_NOT_ZERO: {
                if (pop().asInt().getValue() != 0) {
                    currentLine = jump.getAfterLine();
                }
                break;
            }
        }
        return null;
    }

    @Override
    public Void visit(BcPush push) {
        push(new BcIntValue(push.getValue()));
        return null;
    }

    @Override
    public Void visit(BcPushAddress pushAddress) {
        push(variableAddress.get(pushAddress.getTarget()));
        return null;
    }

    @Override
    public Void visitNop(BcNullaryInstructions nop) {
        // nop
        return null;
    }

    @Override
    public Void visitLoad(BcNullaryInstructions load) {
        BcValue address = pop();
        switch (address.getType()) {
        case PTR:
            push(address.asPtr().getValues()[address.asPtr().getIndex()]);
            break;
        case VAR:
            push(address.asVar().getStack().get(address.asVar().getIndex()));
            break;
        default:
            throw new IllegalArgumentException("Unexpected address type: " + address.getType());
        }
        return null;
    }

    @Override
    public Void visitStore(BcNullaryInstructions store) {
        BcValue value = pop();
        BcValue address = pop();
        switch (address.getType()) {
        case PTR:
            address.asPtr().getValues()[address.asPtr().getIndex()] = value;
            break;
        case VAR:
            address.asVar().getStack().set(address.asVar().getIndex(), value);
            break;
        default:
            throw new IllegalArgumentException("Unexpected value type: " + value.getType());
        }
        return null;
    }

    @Override
    public Void visitReturn(BcNullaryInstructions ret) {
        currentLine = null;
        return null;
    }

    @Override
    public Void visit(BcCall call) {
        BcFunction address = call.getTarget();
        if (program.getFunctions().containsKey(address)) {
            interpret(program.getFunctions().get(address), true);
        } else {
            ExternalFunction function = program.getExternalFunctions().get(address);
            Function<List<BcValue>, BcValue> stub = externalStubs.get(function);
            int paramCount = function.getParameterCount();
            List<BcValue> args = stack.subList(stack.size() - paramCount, stack.size());
            BcValue result = stub.apply(args);
            args.clear();
            if (result != null) {
                push(result);
            }
        }
        return null;
    }

    private BcValue pop() {
        return stack.remove(stack.size() - 1);
    }

    private void push(BcValue value) {
        stack.add(value);
    }
}
