package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgArrayExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgBinaryExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgCastExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgDataExpression;
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
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementList;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgWhileStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcArrayInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcBinOp;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCast;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcDataInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcIndexLoad;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcIndexStore;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJump;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJumpIfZero;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcLoad;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcMemberLoad;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcMemberStore;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcMethodCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcReturn;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcStore;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcStringInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcImmediateValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcNoneValue;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcRegister;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
class BcTranslator implements AsgStatementVisitor<Void>, AsgExpressionVisitor<BcValue> {
    private final BcBuilder builder;

    BcTranslator(BcBuilder builder) {
        this.builder = builder;
    }

    // STATEMENTS
    @Override
    public Void visit(AsgAssignment assignment) {
        BcValue value = assignment.getExpression().accept(this);
        BcInstruction instruction = assignment.getLeftValue().accept(new AsgLeftValueExpressionVisitor<BcInstruction>()
        {
            @Override
            public BcInstruction visit(AsgIndexExpression indexExpression) {
                BcRegister array = (BcRegister) indexExpression.getArray().accept(BcTranslator.this);
                BcValue index = indexExpression.getIndex().accept(BcTranslator.this);
                return new BcIndexStore(array, index, value);
            }

            @Override
            public BcInstruction visit(AsgMemberAccessExpression memberAccessExpression) {
                BcRegister object = (BcRegister) memberAccessExpression.getObject().accept(BcTranslator.this);
                AsgDataType.Field field = memberAccessExpression.getField();
                return new BcMemberStore(object, field, value);
            }

            @Override
            public BcInstruction visit(AsgVariableExpression variableExpression) {
                AsgVariable variable = variableExpression.getVariable();
                builder.useVariable(variable);
                return new BcStore(variable, value);
            }
        });
        builder.write(instruction);
        return null;
    }

    @Override
    public Void visit(AsgIfStatement ifStatement) {
        BcValue condition = ifStatement.getCondition().accept(this);
        BcLabel elseBranch = builder.reserveLabel("else");
        builder.write(new BcJumpIfZero(condition, elseBranch));
        ifStatement.getPositive().accept(this);
        if (ifStatement.getNegative() != null) {
            BcLabel continueBranch = builder.reserveLabel("cont");
            builder.write(new BcJump(continueBranch));
            builder.write(elseBranch);
            ifStatement.getNegative().accept(this);
            builder.write(continueBranch);
        } else {
            builder.write(elseBranch);
        }

        return null;
    }

    @Override
    public Void visit(AsgStatementList statementList) {
        statementList.getStatements().forEach(statement -> statement.accept(this));
        return null;
    }

    @Override
    public Void visit(AsgForStatement forStatement) {
        BcLabel check = builder.reserveLabel("check");
        BcLabel termination = builder.reserveLabel("term");
        forStatement.getInitialization().accept(this);
        builder.write(check);
        builder.write(new BcJumpIfZero(forStatement.getTermination().accept(this), termination));
        forStatement.getBody().accept(this);
        forStatement.getIncrement().accept(this);
        builder.write(new BcJump(check));
        builder.write(termination);
        return null;
    }

    @Override
    public Void visit(AsgWhileStatement whileStatement) {
        BcLabel whileStart = builder.reserveLabel("while");
        BcLabel termination = builder.reserveLabel("term");
        builder.write(whileStart);
        BcValue condition = whileStatement.getCondition().accept(this);
        builder.write(new BcJumpIfZero(condition, termination));
        whileStatement.getBody().accept(this);
        builder.write(new BcJump(whileStart));
        builder.write(termination);
        return null;
    }

    @Override
    public Void visit(AsgRepeatStatement repeatStatement) {
        BcLabel repeatStart = builder.reserveLabel("repeat");
        builder.write(repeatStart);
        repeatStatement.getBody().accept(this);
        BcValue condition = repeatStatement.getCondition().accept(this);
        builder.write(new BcJumpIfZero(condition, repeatStart));
        return null;
    }

    @Override
    public Void visit(AsgExpressionStatement expressionStatement) {
        expressionStatement.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(AsgReturnStatement returnStatement) {
        BcValue value = returnStatement.getValue().accept(this);
        builder.write(new BcReturn(value));
        return null;
    }

    // EXPRESSIONS

    @Override
    public BcValue visit(AsgLiteralExpression<?> literal) {
        switch (literal.getType()) {
        case INT:
            return new BcImmediateValue((Integer) literal.getValue());
        case NONE:
            return BcNoneValue.INSTANCE;
        case STRING:
            return builder.write(new BcStringInit((String) literal.getValue()));
        }
        throw new UnsupportedOperationException("Literal type not supported: " + literal.getType());
    }

    @Override
    public BcValue visit(AsgBinaryExpression binaryExpression) {
        BcValue left = binaryExpression.getLeft().accept(this);
        BcValue right = binaryExpression.getRight().accept(this);
        return builder.write(new BcBinOp(binaryExpression.getOperator(), left, right));
    }

    @Override
    public BcValue visit(AsgFunctionCallExpression functionCall) {
        List<BcValue> arguments = functionCall.getArguments().stream()
            .map(arg -> arg.accept(this))
            .collect(Collectors.toList());
        return builder.write(new BcCall(functionCall.getFunction(), arguments));
    }

    @Override
    public BcValue visit(AsgMethodCallExpression methodCall) {
        BcValue object = methodCall.getObject().accept(this);
        List<BcValue> arguments = methodCall.getArguments().stream()
            .map(arg -> arg.accept(this))
            .collect(Collectors.toList());
        return builder.write(new BcMethodCall((BcRegister) object, methodCall.getMethod(), arguments));
    }

    @Override
    public BcValue visit(AsgArrayExpression arrayExpression) {
        List<BcValue> values = arrayExpression.getValues().stream()
            .map(expr -> expr.accept(this))
            .collect(Collectors.toList());
        return builder.write(new BcArrayInit(values, arrayExpression.getResultType()));
    }

    @Override
    public BcValue visit(AsgIndexExpression indexExpression) {
        return builder.write(new BcIndexLoad(
            (BcRegister) indexExpression.getArray().accept(this),
            indexExpression.getIndex().accept(this)
        ));
    }

    @Override
    public BcValue visit(AsgMemberAccessExpression memberAccessExpression) {
        return builder.write(new BcMemberLoad(
            (BcRegister) memberAccessExpression.getObject().accept(this),
            memberAccessExpression.getField()
        ));
    }

    @Override
    public BcValue visit(AsgVariableExpression variableExpression) {
        AsgVariable variable = variableExpression.getVariable();
        builder.useVariable(variable);
        return builder.write(new BcLoad(variable));
    }

    @Override
    public BcValue visit(AsgCastExpression castExpression) {
        return builder.write(new BcCast(
            castExpression.getExpression().accept(this),
            castExpression.getTarget()
        ));
    }

    @Override
    public BcValue visit(AsgDataExpression dataExpression) {
        return builder.write(new BcDataInit(
            dataExpression.getType(),
            dataExpression.getValues().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().accept(this)
            ))
        ));
    }
}
