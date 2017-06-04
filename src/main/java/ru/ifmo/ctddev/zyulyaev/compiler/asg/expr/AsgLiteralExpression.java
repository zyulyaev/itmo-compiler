package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgPredefinedType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
@Data
public class AsgLiteralExpression<T> implements AsgExpression {
    private final LiteralType type;
    private final T value;

    @Override
    public AsgType getResultType() {
        return type.type;
    }

    @Override
    public <U> U accept(AsgExpressionVisitor<U> visitor) {
        return visitor.visit(this);
    }

    public enum LiteralType {
        INT(AsgPredefinedType.INT),
        STRING(AsgPredefinedType.STRING),
        NONE(AsgPredefinedType.NONE);

        private final AsgType type;

        LiteralType(AsgType type) {
            this.type = type;
        }
    }
}
