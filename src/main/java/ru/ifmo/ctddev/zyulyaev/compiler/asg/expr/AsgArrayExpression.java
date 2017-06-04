package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgArrayType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.List;

/**
 * @author zyulyaev
 * @since 28.05.2017
 */
@Data
public class AsgArrayExpression implements AsgExpression {
    private final List<AsgExpression> values;
    private final AsgArrayType resultType;

    public AsgArrayExpression(List<AsgExpression> values) {
        AsgType compoundType = values.isEmpty() ? AsgPredefinedType.NONE : values.get(0).getResultType();
        if (!values.stream().allMatch(value -> value.getResultType().equals(compoundType))) {
            throw new IllegalArgumentException("Array must contain elements of same type");
        }
        this.values = values;
        this.resultType = new AsgArrayType(compoundType);
    }

    @Override
    public AsgType getResultType() {
        return resultType;
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
