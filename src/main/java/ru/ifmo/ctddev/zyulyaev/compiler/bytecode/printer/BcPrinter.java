package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.printer;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcArrayInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcBinOp;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCast;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcDataInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcIndexLoad;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcIndexStore;
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
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcUnset;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
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

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class BcPrinter implements BcLineVisitor<String>, BcInstructionVisitor<String>, BcValueVisitor<String> {
    private final PrintStream out;

    private BcPrinter(PrintStream out) {
        this.out = out;
    }

    public static void print(BcProgram program, PrintStream out) {
        BcPrinter printer = new BcPrinter(out);
        for (BcMethodDefinition definition : program.getMethods()) {
            printer.printMethod(definition);
            out.println();
        }
        for (BcFunctionDefinition definition : program.getFunctions()) {
            printer.printFunction(definition);
            out.println();
        }
        printer.printFunction(program.getMain());
    }

    private String printVar(AsgVariable variable) {
        return "@" + variable.getName() + ": " + variable.getType();
    }

    private String printParameters(AsgVariable thisValue, List<AsgVariable> parameters) {
        Stream<AsgVariable> all = Stream.concat(
            thisValue == null ? Stream.empty() : Stream.of(thisValue),
            parameters.stream()
        );
        return all.map(this::printVar)
            .collect(Collectors.joining(", ", "(", ")"));
    }

    private void printLocalVariables(List<AsgVariable> variables) {
        if (!variables.isEmpty()) {
            out.println("var " + variables.stream()
                .map(this::printVar)
                .collect(Collectors.joining(",\n\t", "", ";")));
        }
    }

    private void printBody(List<BcLine> body) {
        out.println("begin");
        body.stream()
            .map(this::visit)
            .forEach(out::println);
        out.println("end");
    }

    private void printMethod(BcMethodDefinition definition) {
        AsgMethod method = definition.getMethod();
        List<AsgVariable> parameters = definition.getParameters();
        out.println("define " + method.getParent().getName() + "." + method.getName() +
            printParameters(definition.getThisValue(), parameters) + ": " + method.getReturnType());
        printLocalVariables(definition.getLocalVariables());
        printBody(definition.getBody());
    }

    private void printFunction(BcFunctionDefinition definition) {
        AsgFunction function = definition.getFunction();
        List<AsgVariable> parameters = definition.getParameters();
        out.println("define " + function.getName() +
            printParameters(null, parameters) + ": " + function.getReturnType());
        printLocalVariables(definition.getLocalVariables());
        printBody(definition.getBody());
    }

    private String visit(BcLine line) {
        return line.accept(this);
    }

    private String visit(BcValue value) {
        return value.accept(this);
    }

    @Override
    public String visit(BcArrayInit arrayInit) {
        return "array_init " + arrayInit.getValues().stream()
            .map(this::visit)
            .collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public String visit(BcStringInit stringInit) {
        return "string_init \"" + stringInit.getValue().replaceAll("[\\\\\"]", "\\$1") + "\"";
    }

    @Override
    public String visit(BcDataInit dataInit) {
        return "data_init " + dataInit.getType().getName() + " " + dataInit.getValues().entrySet().stream()
            .map(entry -> entry.getKey().getName() + ": " + entry.getValue().accept(this))
            .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public String visit(BcBinOp binOp) {
        return binOp.getLeft().accept(this) + " " + binOp.getOperator().getTextual() + " " + binOp.getRight().accept(this);
    }

    @Override
    public String visit(BcJump jump) {
        return "jmp " + jump.getLabel().getName();
    }

    @Override
    public String visit(BcJumpIfZero jump) {
        String target = jump.getLabel().getName();
        return "jz " + jump.getCondition().accept(this) + " " + target;
    }

    @Override
    public String visit(BcCall call) {
        return "call " + call.getFunction().getName() + call.getArguments().stream()
            .map(this::visit)
            .collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public String visit(BcMethodCall call) {
        return "mcall " + call.getObject().accept(this) + " " +
            call.getMethod().getParent().getName() + "." + call.getMethod().getName() +
            call.getArguments().stream()
                .map(this::visit)
                .collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public String visit(BcStore store) {
        return "store @" + store.getVariable().getName() + " " + store.getValue().accept(this);
    }

    @Override
    public String visit(BcLoad load) {
        return "load @" + load.getVariable().getName();
    }

    @Override
    public String visit(BcUnset unset) {
        return "unset " + unset.getVariable().getName();
    }

    @Override
    public String visit(BcIndexLoad indexLoad) {
        return "iload " + indexLoad.getArray().accept(this) + " " + indexLoad.getIndex().accept(this);
    }

    @Override
    public String visit(BcIndexStore indexStore) {
        return "istore " + indexStore.getArray().accept(this) +
            " " + indexStore.getIndex().accept(this) +
            " " + indexStore.getValue().accept(this);
    }

    @Override
    public String visit(BcMemberLoad memberLoad) {
        AsgDataType.Field field = memberLoad.getField();
        return "mload " + field.getParent().getName() + "." + field.getName() + " " + memberLoad.getObject().accept(this);
    }

    @Override
    public String visit(BcMemberStore memberStore) {
        AsgDataType.Field field = memberStore.getField();
        return "mstore " + field.getParent().getName() + "." + field.getName() +
            " " + memberStore.getObject().accept(this) +
            " " + memberStore.getValue().accept(this);
    }

    @Override
    public String visit(BcReturn ret) {
        return "return " + ret.getValue().accept(this);
    }

    @Override
    public String visit(BcCast cast) {
        return "cast " + cast.getTarget() + " " + cast.getValue().accept(this);
    }

    @Override
    public String visit(BcInstructionLine instructionLine) {
        String destination = "";
        if (instructionLine.getDestination() != null) {
            destination = instructionLine.getDestination().accept(this) + " = ";
        }
        return "\t" + destination + instructionLine.getInstruction().accept(this);
    }

    @Override
    public String visit(BcLabelLine labelLine) {
        return labelLine.getLabel().getName() + ":";
    }

    @Override
    public String visit(BcImmediateValue value) {
        return "$" + value.getValue();
    }

    @Override
    public String visit(BcNoneValue value) {
        return "none";
    }

    @Override
    public String visit(BcRegister register) {
        return "%" + register.getIndex();
    }
}
