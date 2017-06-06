package ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.line;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.instruction.BcInstruction;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.value.BcRegister;

import javax.annotation.Nullable;

/**
 * @author zyulyaev
 * @since 31.05.2017
 */
@Getter
@ToString
@AllArgsConstructor
public class BcInstructionLine implements BcLine {
    @Nullable
    private final BcRegister destination;
    private final BcInstruction instruction;

    @Override
    public <T> T accept(BcLineVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
