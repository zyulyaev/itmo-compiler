package ru.ifmo.ctddev.zyulyaev.compiler.asg.expr;

import lombok.Data;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgType;

/**
 * @author zyulyaev
 * @since 04.06.2017
 */
@Data
public class AsgMemberAccessExpression implements AsgLeftValueExpression {
    private final AsgExpression object;
    private final AsgDataType.Field field;

    public AsgMemberAccessExpression(AsgExpression object, String member) {
        this.object = object;
        AsgType objectType = object.getResultType();
        if (objectType instanceof AsgDataType) {
            this.field = ((AsgDataType) objectType).getFields().stream()
                .filter(f -> member.equals(f.getName()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Member not found: " + member));
        } else {
            throw new IllegalArgumentException("Members can be accessed only on data types");
        }
    }

    @Override
    public AsgType getResultType() {
        return field.getType();
    }

    @Override
    public <T> T accept(AsgExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
