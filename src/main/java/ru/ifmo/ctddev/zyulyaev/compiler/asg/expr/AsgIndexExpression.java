package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgArrayType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgTypeUtils;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
@Data
public class AsgIndexExpression implements AsgLeftValueExpression {
    private final AsgExpression array;
    private final AsgExpression index;

    public AsgIndexExpression(AsgExpression array, AsgExpression index) {
        if (!(array.getResultType() instanceof AsgArrayType)) {
            throw new IllegalArgumentException("Cannot get index of non-array type: " + array.getResultType());
        }
        this.array = array;
        this.index = index;
    }

    @Override
    public AsgType getResultType() {
        return AsgTypeUtils.getCompoundType(array.getResultType(), 1);
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
