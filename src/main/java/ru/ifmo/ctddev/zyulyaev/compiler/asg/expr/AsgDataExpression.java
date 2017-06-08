package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

import java.util.Map;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
@Data
public class AsgDataExpression implements AsgExpression {
    private final AsgDataType type;
    private final Map<AsgDataType.Field, AsgExpression> values;

    public AsgDataExpression(AsgDataType type, Map<AsgDataType.Field, AsgExpression> values) {
        if (!type.getFields().stream().allMatch(values::containsKey)) {
            throw new IllegalArgumentException("Not all fields are set");
        }
        if (values.entrySet().stream()
            .anyMatch(entry -> !entry.getKey().getType().equals(entry.getValue().getResultType())))
        {
            throw new IllegalArgumentException("Types don't match");
        }
        this.type = type;
        this.values = values;
    }

    @Override
    public AsgType getResultType() {
        return type;
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
