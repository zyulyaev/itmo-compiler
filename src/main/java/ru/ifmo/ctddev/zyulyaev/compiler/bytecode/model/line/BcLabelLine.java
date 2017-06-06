package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcLabel;

/**
 * @author zyulyaev
 * @since 31.05.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class BcLabelLine implements BcLine {
    private final BcLabel label;

    @Override
    public <T> T accept(BcLineVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
