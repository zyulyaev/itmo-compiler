package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
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
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcUnset;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcMethodDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcInstructionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line.BcLineVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcImmediateValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcNoneValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValueVisitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcInterpreter {
    private final Map<AsgFunction, BcFunctionDefinition> functionDefinitions;
    private final Map<AsgFunction, Function<List<Value>, Value>> externalStubs;
    private final Table<AsgDataType, AsgMethod, BcMethodDefinition> methodDefinitions;
    private final BcFunctionDefinition main;

    public BcInterpreter(BcProgram program, Map<AsgExternalFunction, Function<List<Value>, Value>> externalStubs) {
        this.functionDefinitions = program.getFunctions().stream()
            .collect(Collectors.toMap(BcFunctionDefinition::getFunction, Function.identity()));
        this.methodDefinitions = program.getMethods().stream()
            .collect(Collector.of(
                HashBasedTable::create,
                (table, def) -> table.put(def.getDataType(), def.getMethod(), def),
                (a, b) -> {
                    a.putAll(b);
                    return a;
                }
            ));
        this.externalStubs = program.getExternalFunctions().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> externalStubs.get(entry.getValue())
            ));
        main = program.getMain();
    }

    public void interpret() {
        new Frame(main, Collections.emptyList()).interpret();
    }

    private static Map<BcLabel, Integer> buildLabelMap(List<BcLine> body) {
        Map<BcLabel, Integer> labelMap = new HashMap<>();
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
        return labelMap;
    }

    private class Frame implements BcLineVisitor<Value>, BcInstructionVisitor<Value>, BcValueVisitor<Value> {
        private final List<BcLine> body;
        private final Map<AsgVariable, Value> variableValues = new HashMap<>();
        private final Map<BcRegister, Value> registerValues = new HashMap<>();
        private final Map<BcLabel, Integer> labelMap;
        private int currentLine;

        private Frame(BcMethodDefinition definition, Value thisValue, List<Value> arguments) {
            List<AsgVariable> parameters = definition.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                variableValues.put(parameters.get(i), arguments.get(i));
            }
            variableValues.put(definition.getThisValue(), thisValue);
            this.body = definition.getBody();
            this.labelMap = buildLabelMap(definition.getBody());
        }

        private Frame(BcFunctionDefinition definition, List<Value> arguments) {
            List<AsgVariable> parameters = definition.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                variableValues.put(parameters.get(i), arguments.get(i));
            }
            this.body = definition.getBody();
            this.labelMap = buildLabelMap(definition.getBody());
        }

        private Value interpret() {
            for (currentLine = 0; currentLine < body.size(); currentLine++) {
                Value returnValue = body.get(currentLine).accept(this);
                if (returnValue != null) {
                    return returnValue;
                }
            }
            return new IntValue(0);
        }

        @Override
        public Value visit(BcArrayInit arrayInit) {
            List<Value> values = arrayInit.getValues().stream()
                .map(val -> val.accept(this))
                .collect(Collectors.toList());
            return new ArrayValue(values.toArray(new Value[0]));
        }

        @Override
        public Value visit(BcStringInit stringInit) {
            return new StringValue(stringInit.getValue().toCharArray());
        }

        @Override
        public Value visit(BcDataInit dataInit) {
            Map<AsgDataType.Field, Value> values = dataInit.getValues().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().accept(this)
            ));
            return new DataValue(dataInit.getType(), values);
        }

        @Override
        public Value visit(BcBinOp binOp) {
            return Operators.apply(
                binOp.getLeft().accept(this),
                binOp.getRight().accept(this),
                binOp.getOperator()
            );
        }

        @Override
        public Value visit(BcJump jump) {
            currentLine = labelMap.get(jump.getLabel());
            return null;
        }

        @Override
        public Value visit(BcJumpIfZero jump) {
            int target = labelMap.get(jump.getLabel());
            Value condition = jump.getCondition().accept(this);
            if (condition.asInt().getValue() == 0) {
                currentLine = target;
            }
            return null;
        }

        @Override
        public Value visit(BcCall call) {
            List<Value> arguments = call.getArguments().stream()
                .map(value -> value.accept(this))
                .collect(Collectors.toList());
            AsgFunction target = call.getFunction();
            BcFunctionDefinition definition = functionDefinitions.get(target);
            if (definition != null) {
                return new Frame(definition, arguments).interpret();
            } else {
                return externalStubs.get(target).apply(arguments);
            }
        }

        @Override
        public Value visit(BcMethodCall call) {
            Value object = call.getObject().accept(this);
            List<Value> arguments = call.getArguments().stream()
                .map(value -> value.accept(this))
                .collect(Collectors.toList());
            BcMethodDefinition definition =
                methodDefinitions.get(object.asData().getDataType(), call.getMethod());
            return new Frame(definition, object, arguments).interpret();
        }

        @Override
        public Value visit(BcStore store) {
            Value value = store.getValue().accept(this);
            variableValues.put(store.getVariable(), value);
            return null;
        }

        @Override
        public Value visit(BcLoad load) {
            return variableValues.get(load.getVariable());
        }

        @Override
        public Value visit(BcIndexLoad indexLoad) {
            Value array = indexLoad.getArray().accept(this);
            Value index = indexLoad.getIndex().accept(this);
            return array.asArray().get(index.asInt().getValue());
        }

        @Override
        public Value visit(BcIndexStore indexStore) {
            Value array = indexStore.getArray().accept(this);
            Value index = indexStore.getIndex().accept(this);
            Value value = indexStore.getValue().accept(this);
            array.asArray().set(index.asInt().getValue(), value);
            return null;
        }

        @Override
        public Value visit(BcMemberLoad memberLoad) {
            Value object = memberLoad.getObject().accept(this);
            AsgDataType.Field field = memberLoad.getField();
            return object.asData().get(field);
        }

        @Override
        public Value visit(BcMemberStore memberStore) {
            Value object = memberStore.getObject().accept(this);
            AsgDataType.Field field = memberStore.getField();
            Value value = memberStore.getValue().accept(this);
            object.asData().set(field, value);
            return null;
        }

        @Override
        public Value visit(BcReturn ret) {
            return ret.getValue().accept(this);
        }

        @Override
        public Value visit(BcCast cast) {
            return cast.getValue().accept(this);
        }

        @Override
        public Value visit(BcUnset unset) {
            // todo
            return null;
        }

        @Override
        public Value visit(BcInstructionLine instructionLine) {
            BcRegister destination = instructionLine.getDestination();
            BcInstruction instruction = instructionLine.getInstruction();
            Value value = instruction.accept(this);
            if (instruction instanceof BcReturn) {
                return value;
            } else {
                registerValues.put(destination, value);
                return null;
            }
        }

        @Override
        public Value visit(BcLabelLine labelLine) {
            // nothing to do
            return null;
        }

        @Override
        public Value visit(BcImmediateValue value) {
            return new IntValue(value.getValue());
        }

        @Override
        public Value visit(BcNoneValue value) {
            return new IntValue(0);
        }

        @Override
        public Value visit(BcRegister register) {
            return registerValues.get(register);
        }
    }
}
