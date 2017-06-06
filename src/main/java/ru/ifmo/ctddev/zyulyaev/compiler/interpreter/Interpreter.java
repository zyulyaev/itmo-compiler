package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgExternalFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunction;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgImplDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethodDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgArrayExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgBinaryExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgCastExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgDataExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpressionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgFunctionCallExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgIndexExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLeftValueExpressionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLiteralExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgMemberAccessExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgMethodCallExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgVariableExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgAssignment;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgExpressionStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgForStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgIfStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgRepeatStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgReturnStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementList;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgWhileStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.ArrayIndexValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.ArrayValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.DataTypeValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.IntValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.LeftValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.MemberValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.NoneValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.RightValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.StringValue;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.VarValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class Interpreter {
    private final Map<AsgFunction, AsgFunctionDefinition> definitionMap;
    private final Table<AsgDataType, AsgClassType, AsgImplDefinition> implDefinitionTable;
    private final Map<AsgFunction, Function<List<RightValue>, RightValue>> externalStubs;
    private final AsgStatement main;

    public Interpreter(AsgProgram program,
        Map<AsgExternalFunction, Function<List<RightValue>, RightValue>> externalStubs)
    {
        this.definitionMap = program.getFunctionDefinitions().stream()
            .collect(Collectors.toMap(AsgFunctionDefinition::getFunction, Function.identity()));
        this.implDefinitionTable = program.getImplDefinitions().stream()
            .collect(Collector.of(
                HashBasedTable::create,
                (table, def) -> table.put(def.getDataType(), def.getClassType(), def),
                (left, right) -> {
                    left.putAll(right);
                    return left;
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
        new Frame(main).interpret();
    }

    private class Frame implements AsgStatementVisitor<Value>, AsgExpressionVisitor<Value> {
        private final Map<AsgVariable, LeftValue> valuesMap = new HashMap<>();
        private final AsgStatement body;

        private Frame(AsgStatement body) {
            this.body = body;
        }

        private Frame(AsgFunctionDefinition definition, List<RightValue> arguments) {
            this.body = definition.getBody();
            for (int i = 0; i < arguments.size(); i++) {
                valuesMap.put(definition.getParameters().get(i), new VarValue(arguments.get(i)));
            }
        }

        private Frame(AsgMethodDefinition definition, RightValue thisValue, List<RightValue> arguments) {
            this.body = definition.getBody();
            for (int i = 0; i < arguments.size(); i++) {
                valuesMap.put(definition.getParameters().get(i), new VarValue(arguments.get(i)));
            }
            valuesMap.put(definition.getThisValue(), new VarValue(thisValue));
        }

        RightValue interpret() {
            Value result = body.accept(this);
            return result == null ? NoneValue.INSTANCE : result.asRightValue();
        }

        private LeftValue getOrDefineValue(AsgVariable variable) {
            return valuesMap.computeIfAbsent(variable, $ -> new VarValue(null));
        }

        private RightValue callFunction(AsgFunction function, List<RightValue> arguments) {
            if (definitionMap.containsKey(function)) {
                return new Frame(definitionMap.get(function), arguments).interpret();
            } else if (externalStubs.containsKey(function)) {
                return externalStubs.get(function).apply(arguments);
            } else {
                throw new IllegalArgumentException("Function not found: " + function);
            }
        }

        private RightValue callMethod(DataTypeValue object, AsgMethod method, List<RightValue> arguments) {
            AsgImplDefinition implementation = implDefinitionTable.get(object.getType(), method.getParent());
            AsgMethodDefinition definition = implementation.getDefinition(method);
            return new Frame(definition, object, arguments).interpret();
        }

        // STATEMENTS

        @Override
        public Value visit(AsgAssignment assignment) {
            LeftValue leftValue = assignment.getLeftValue().accept(new AsgLeftValueExpressionVisitor<LeftValue>() {
                @Override
                public LeftValue visit(AsgIndexExpression indexExpression) {
                    return indexExpression.accept(Frame.this).asLeftValue();
                }

                @Override
                public LeftValue visit(AsgMemberAccessExpression memberAccessExpression) {
                    return memberAccessExpression.accept(Frame.this).asLeftValue();
                }

                @Override
                public LeftValue visit(AsgVariableExpression variableExpression) {
                    return getOrDefineValue(variableExpression.getVariable());
                }
            });
            leftValue.set(assignment.getExpression().accept(this).asRightValue());
            return null;
        }

        @Override
        public Value visit(AsgIfStatement ifStatement) {
            if (interpretCondition(ifStatement.getCondition())) {
                return ifStatement.getPositive().accept(this);
            } else if (ifStatement.getNegative() != null) {
                return ifStatement.getNegative().accept(this);
            }
            return null;
        }

        @Override
        public Value visit(AsgStatementList statementList) {
            for (AsgStatement statement : statementList.getStatements()) {
                Value result = statement.accept(this);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        @Override
        public Value visit(AsgForStatement forStatement) {
            forStatement.getInitialization().accept(this);
            while (this.interpretCondition(forStatement.getTermination())) {
                Value result = forStatement.getBody().accept(this);
                if (result != null) {
                    return result;
                }
                forStatement.getIncrement().accept(this);
            }
            return null;
        }

        @Override
        public Value visit(AsgWhileStatement whileStatement) {
            while (interpretCondition(whileStatement.getCondition())) {
                Value result = whileStatement.getBody().accept(this);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        @Override
        public Value visit(AsgRepeatStatement repeatStatement) {
            do {
                Value result = repeatStatement.getBody().accept(this);
                if (result != null) {
                    return result;
                }
            } while (!interpretCondition(repeatStatement.getCondition()));
            return null;
        }

        @Override
        public Value visit(AsgExpressionStatement expressionStatement) {
            expressionStatement.getExpression().accept(this);
            return null;
        }

        @Override
        public Value visit(AsgReturnStatement returnStatement) {
            return returnStatement.getValue().accept(this);
        }

        private boolean interpretCondition(AsgExpression expression) {
            IntValue value = expression.accept(this).asRightValue().asInt();
            return value.getValue() != 0;
        }

        // EXPRESSIONS

        @Override
        public Value visit(AsgLiteralExpression<?> literal) {
            switch (literal.getType()) {
            case INT:
                return new IntValue((Integer) literal.getValue());
            case STRING:
                return new StringValue(((String) literal.getValue()).toCharArray());
            case NONE:
                return NoneValue.INSTANCE;
            }
            throw new IllegalArgumentException("Unexpected literal type: " + literal.getType());
        }

        @Override
        public Value visit(AsgBinaryExpression binaryExpression) {
            Value left = binaryExpression.getLeft().accept(this);
            Value right = binaryExpression.getRight().accept(this);
            return Operators.apply(left.asRightValue(), right.asRightValue(), binaryExpression.getOperator());
        }

        @Override
        public Value visit(AsgFunctionCallExpression functionCall) {
            AsgFunction function = functionCall.getFunction();
            List<RightValue> arguments = functionCall.getArguments().stream()
                .map(arg -> arg.accept(this).asRightValue())
                .collect(Collectors.toList());
            return callFunction(function, arguments);
        }

        @Override
        public Value visit(AsgMethodCallExpression methodCall) {
            DataTypeValue object = methodCall.getObject().accept(this).asRightValue().asDataType();
            List<RightValue> arguments = methodCall.getArguments().stream()
                .map(arg -> arg.accept(this).asRightValue())
                .collect(Collectors.toList());
            return callMethod(object, methodCall.getMethod(), arguments);
        }

        @Override
        public Value visit(AsgArrayExpression arrayExpression) {
            RightValue[] values = arrayExpression.getValues().stream()
                .map(value -> value.accept(this).asRightValue())
                .toArray(RightValue[]::new);
            return new ArrayValue(values);
        }

        @Override
        public Value visit(AsgIndexExpression indexExpression) {
            ArrayValue array = indexExpression.getArray().accept(this).asRightValue().asArray();
            IntValue index = indexExpression.getIndex().accept(this).asRightValue().asInt();
            return new ArrayIndexValue(array, index.getValue());
        }

        @Override
        public Value visit(AsgMemberAccessExpression memberAccessExpression) {
            DataTypeValue object = memberAccessExpression.getObject().accept(this).asRightValue().asDataType();
            AsgDataType.Field field = memberAccessExpression.getField();
            return new MemberValue(object, field);
        }

        @Override
        public Value visit(AsgVariableExpression variableExpression) {
            return valuesMap.get(variableExpression.getVariable());
        }

        @Override
        public Value visit(AsgCastExpression castExpression) {
            return castExpression.getExpression().accept(this);
        }

        @Override
        public Value visit(AsgDataExpression dataExpression) {
            Map<AsgDataType.Field, RightValue> values = dataExpression.getValues().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().accept(this).asRightValue()
                ));
            return new DataTypeValue(dataExpression.getType(), values);
        }
    }
}
