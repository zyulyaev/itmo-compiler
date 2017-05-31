package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgArrayExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgBinaryExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgExpressionVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgFunctionCallExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLeftValueExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.expr.AsgLiteralExpression;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgAssignment;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgForStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgFunctionCallStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgIfStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgRepeatStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgReturnStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementList;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgWhileStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcArrayInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcBinOp;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJump;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcNullaryInstructions;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPush;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPushAddress;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcStringInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcUnset;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;

import java.io.Closeable;
import java.util.List;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcTranslator implements AsgStatementVisitor<Void>, AsgExpressionVisitor<Void>, Closeable {
    private final BcContext context;
    private final BcOutput output;

    public BcTranslator(BcContext context, BcOutput output) {
        this.context = context;
        this.output = output;
    }

    private BcTranslator createChild() {
        return new BcTranslator(new BcContext(context), output);
    }

    private void translateNested(AsgExpression expression) {
        try (BcTranslator child = createChild()) {
            expression.accept(child);
        }
    }

    private void translateNested(AsgStatement statement) {
        try (BcTranslator child = createChild()) {
            statement.accept(child);
        }
    }

    // STATEMENTS
    @Override
    public Void visit(AsgAssignment assignment) {
        translateLeftValue(assignment.getLeftValue());
        try (BcTranslator child = createChild()) {
            assignment.getExpression().accept(child);
            output.write(BcNullaryInstructions.STORE);
        }
        return null;
    }

    @Override
    public Void visit(AsgIfStatement ifStatement) {
        translateNested(ifStatement.getCondition());
        BcLabel elseBranch = context.reserveLabel("else");
        output.write(new BcJump(BcJump.Condition.IF_ZERO, elseBranch));
        translateNested(ifStatement.getPositive());

        if (ifStatement.getNegative() != null) {
            BcLabel continueBranch = context.reserveLabel("cont");
            output.write(new BcJump(BcJump.Condition.ALWAYS, continueBranch));
            output.write(elseBranch);
            translateNested(ifStatement.getNegative());
            output.write(continueBranch);
        } else {
            output.write(elseBranch);
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
        try (BcTranslator outerChild = createChild()) {
            forStatement.getInitialization().accept(outerChild);
            BcLabel check = context.reserveLabel("check");
            output.write(check);
            forStatement.getTermination().accept(outerChild);
            BcLabel termination = context.reserveLabel("term");
            output.write(new BcJump(BcJump.Condition.IF_ZERO, termination));
            outerChild.translateNested(forStatement.getBody());
            forStatement.getIncrement().accept(outerChild);
            output.write(new BcJump(BcJump.Condition.ALWAYS, check));
            output.write(termination);
        }
        return null;
    }

    @Override
    public Void visit(AsgWhileStatement whileStatement) {
        BcLabel whileStart = context.reserveLabel("while");
        output.write(whileStart);
        translateNested(whileStatement.getCondition());
        BcLabel termination = context.reserveLabel("term");
        output.write(new BcJump(BcJump.Condition.IF_ZERO, termination));
        translateNested(whileStatement.getBody());
        output.write(new BcJump(BcJump.Condition.ALWAYS, whileStart));
        output.write(termination);
        return null;
    }

    @Override
    public Void visit(AsgRepeatStatement repeatStatement) {
        BcLabel repeatStart = context.reserveLabel("repeat");
        output.write(repeatStart);
        try (BcTranslator child = createChild()) {
            repeatStatement.getBody().accept(child);
            repeatStatement.getCondition().accept(child);
        }
        output.write(new BcJump(BcJump.Condition.IF_ZERO, repeatStart));
        return null;
    }

    @Override
    public Void visit(AsgFunctionCallStatement functionCallStatement) {
        translateNested(functionCallStatement.getExpression());
        output.write(BcNullaryInstructions.POP);
        return null;
    }

    @Override
    public Void visit(AsgReturnStatement returnStatement) {
        translateNested(returnStatement.getValue());
        output.write(new BcJump(BcJump.Condition.ALWAYS, context.getReturnLabel()));
        return null;
    }

    // EXPRESSIONS

    @Override
    public Void visit(AsgLiteralExpression<?> literal) {
        switch (literal.getType()) {
        case INT:
            output.write(new BcPush((Integer) literal.getValue()));
            return null;
        case NULL:
            output.write(new BcPush(0));
            return null;
        case STRING:
            BcVariable tmp = context.reserveVariable();
            output.write(new BcStringInit(tmp, (String) literal.getValue()));
            output.write(new BcPushAddress(tmp));
            output.write(BcNullaryInstructions.LOAD);
            return null;
        }
        throw new UnsupportedOperationException("Literal type not supported: " + literal.getType());
    }

    @Override
    public Void visit(AsgLeftValueExpression leftValue) {
        translateLeftValue(leftValue);
        output.write(BcNullaryInstructions.LOAD);
        return null;
    }

    @Override
    public Void visit(AsgBinaryExpression binaryExpression) {
        binaryExpression.getLeft().accept(this);
        binaryExpression.getRight().accept(this);
        output.write(new BcBinOp(binaryExpression.getOperator()));
        return null;
    }

    @Override
    public Void visit(AsgFunctionCallExpression functionCall) {
        List<AsgExpression> args = functionCall.getArguments();
        int argsCount = args.size();
        for (int i = argsCount - 1; i >= 0; i--) {
            args.get(i).accept(this);
        }
        output.write(new BcCall(context.getFunction(functionCall.getFunction())));
        return null;
    }

    @Override
    public Void visit(AsgArrayExpression arrayExpression) {
        List<AsgExpression> values = arrayExpression.getValues();
        for (int i = values.size() - 1; i >= 0; i--) {
            values.get(i).accept(this);
        }
        BcVariable tmp = context.reserveVariable();
        output.write(new BcArrayInit(tmp, values.size()));
        output.write(new BcPushAddress(tmp));
        output.write(BcNullaryInstructions.LOAD);
        return null;
    }

    private void translateLeftValue(AsgLeftValueExpression leftValue) {
        BcVariable variable = context.getVariable(leftValue.getVariable());
        output.write(new BcPushAddress(variable));
        for (AsgExpression index : leftValue.getIndexes()) {
            output.write(BcNullaryInstructions.LOAD);
            translateNested(index);
            output.write(BcNullaryInstructions.INDEX);
        }
    }

    @Override
    public void close() {
        context.cleanup().forEach(var -> output.write(new BcUnset(var)));
    }
}
