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
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementList;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgStatementVisitor;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.stmt.AsgWhileStatement;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcArrayInit;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcBinOp;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcCall;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcJump;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcNullaryInstructions;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPush;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcPushAddress;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcVariable;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.BinaryOperator;

import java.util.List;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
public class BcFunctionTranslator implements AsgStatementVisitor<BcLine>, AsgExpressionVisitor<BcLine> {
    private final BcFunctionContext context;
    private final BcOutput output;

    public BcFunctionTranslator(BcFunctionContext context, BcOutput output) {
        this.context = context;
        this.output = output;
    }

    // STATEMENTS
    @Override
    public BcLine visit(AsgAssignment assignment) {
        translateLeftValue(assignment.getLeftValue());
        assignment.getExpression().accept(this);
        return output.write(BcNullaryInstructions.STORE);
    }

    @Override
    public BcLine visit(AsgIfStatement ifStatement) {
        ifStatement.getCondition().accept(this);
        BcDummy jumpElseDummy = output.dummy();
        BcLine positiveEnd = ifStatement.getPositive().accept(this);
        if (ifStatement.getNegative() != null) {
            BcDummy jumpContinueDummy = output.dummy();
            BcLine negativeEnd = ifStatement.getNegative().accept(this);
            BcLine continueEnd = jumpContinueDummy.replace(new BcJump(BcJump.Condition.ALWAYS, negativeEnd));
            jumpElseDummy.replace(new BcJump(BcJump.Condition.IF_ZERO, continueEnd));
            return negativeEnd;
        } else {
            jumpElseDummy.replace(new BcJump(BcJump.Condition.IF_ZERO, positiveEnd));
            return positiveEnd;
        }
    }

    @Override
    public BcLine visit(AsgStatementList statementList) {
        return statementList.getStatements().stream()
            .map(statement -> statement.accept(this))
            .reduce((a, b) -> b)
            .orElseGet(() -> output.write(BcNullaryInstructions.NOP));
    }

    @Override
    public BcLine visit(AsgForStatement forStatement) {
        BcLine initLast = forStatement.getInitialization().accept(this);
        forStatement.getTermination().accept(this);
        BcDummy terminationJumpDummy = output.dummy();
        forStatement.getBody().accept(this);
        forStatement.getIncrement().accept(this);
        BcLine result = output.write(new BcJump(BcJump.Condition.ALWAYS, initLast));
        terminationJumpDummy.replace(new BcJump(BcJump.Condition.IF_ZERO, result));
        return result;
    }

    @Override
    public BcLine visit(AsgWhileStatement whileStatement) {
        BcLine whileStart = output.write(BcNullaryInstructions.NOP);// todo workaround jump ON or AFTER
        whileStatement.getCondition().accept(this);
        BcDummy terminationJumpDummy = output.dummy();
        whileStatement.getBody().accept(this);
        BcLine result = output.write(new BcJump(BcJump.Condition.ALWAYS, whileStart));
        terminationJumpDummy.replace(new BcJump(BcJump.Condition.IF_ZERO, result));
        return result;
    }

    @Override
    public BcLine visit(AsgRepeatStatement repeatStatement) {
        BcLine repeatStart = output.write(BcNullaryInstructions.NOP);// todo workaround jump ON or AFTER
        repeatStatement.getBody().accept(this);
        repeatStatement.getCondition().accept(this);
        return output.write(new BcJump(BcJump.Condition.IF_ZERO, repeatStart));
    }

    @Override
    public BcLine visit(AsgFunctionCallStatement functionCallStatement) {
        functionCallStatement.getExpression().accept(this);
        return output.write(BcNullaryInstructions.POP);
    }

    @Override
    public BcLine visit(AsgReturnStatement returnStatement) {
        returnStatement.getValue().accept(this);
        return output.write(BcNullaryInstructions.RETURN);
    }

    // EXPRESSIONS

    @Override
    public BcLine visit(AsgLiteralExpression<?> literal) {
        switch (literal.getType()) {
        case INT: return output.write(new BcPush((Integer) literal.getValue()));
        case NULL: return output.write(new BcPush(0));
        case STRING:
            String value = (String) literal.getValue();
            for (char c : value.toCharArray()) {
                output.write(new BcPush(c));
            }
            return output.write(new BcArrayInit(value.length()));
        }
        throw new UnsupportedOperationException("Literal type not supported: " + literal.getType());
    }

    @Override
    public BcLine visit(AsgLeftValueExpression leftValue) {
        translateLeftValue(leftValue);
        return output.write(BcNullaryInstructions.LOAD);
    }

    @Override
    public BcLine visit(AsgBinaryExpression binaryExpression) {
        binaryExpression.getLeft().accept(this);
        binaryExpression.getRight().accept(this);
        return output.write(new BcBinOp(binaryExpression.getOperator()));
    }

    @Override
    public BcLine visit(AsgFunctionCallExpression functionCall) {
        List<AsgExpression> args = functionCall.getArguments();
        int argsCount = args.size();
        for (int i = argsCount - 1; i >= 0; i--) {
            args.get(i).accept(this);
        }
        return output.write(new BcCall(context.getFunction(functionCall.getFunction())));
    }

    @Override
    public BcLine visit(AsgArrayExpression arrayExpression) {
        List<AsgExpression> values = arrayExpression.getValues();
        for (AsgExpression expression : values) {
            expression.accept(this);
        }
        return output.write(new BcArrayInit(values.size()));
    }

    private BcLine translateLeftValue(AsgLeftValueExpression leftValue) {
        BcVariable variable = context.getVariable(leftValue.getVariable());
        BcLine result = output.write(new BcPushAddress(variable));
        for (AsgExpression index : leftValue.getIndexes()) {
            output.write(BcNullaryInstructions.LOAD);
            index.accept(this);
            result = output.write(new BcBinOp(BinaryOperator.ADD));
        }
        return result;
    }
}
