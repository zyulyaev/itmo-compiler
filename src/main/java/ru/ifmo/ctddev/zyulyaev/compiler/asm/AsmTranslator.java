package ru.ifmo.ctddev.zyulyaev.compiler.asm;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgMethod;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgClassType;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.type.AsgDataType;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmAsciiDirective;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmGlobl;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmIntDirective;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLabelLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmSectionLine;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.operand.AsmSymbol;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcFunctionDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcMethodDefinition;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;

import java.util.List;
import java.util.Set;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public class AsmTranslator {
    private final BcProgram program;
    private final Environment env;
    private final GarbageCollector gc;

    public AsmTranslator(BcProgram program) {
        this.program = program;
        this.env = new Environment(program.getDataDefinitions(), program.getClassDefinitions(),
            program.getFunctions(), program.getExternalFunctions(), program.getMethods(),
            program.getMain().getFunction());
        this.gc = new GarbageCollector(this.env);
        collectStrings(program).forEach(env::cacheString);
    }

    public List<AsmLine> translate() {
        AsmOutput output = new AsmOutput();

        // Data section
        output.write(AsmSectionLine.DATA);
        for (String str : env.getCachedStrings()) {
            output.write(env.getStringSymbol(str));
            output.write(new AsmAsciiDirective(str));
        }

        // Text section
        output.write(AsmSectionLine.TEXT);
        for (AsgDataType dataType : program.getDataDefinitions()) {
            for (AsgClassType classType : dataType.getImplementedClasses()) {
                AsmSymbol symbol = env.getVTableSymbol(dataType, classType);
                output.write(new AsmLabelLine(symbol));
                output.write(new AsmIntDirective(env.getDestructorSymbol(dataType)));
                for (AsgMethod method : classType.getMethods()) {
                    output.write(new AsmIntDirective(env.getMethodSymbol(dataType, method)));
                }
            }
        }

        output.write(new AsmGlobl(env.main));
        program.getFunctions().forEach(definition -> translateFunction(definition, output));
        program.getMethods().forEach(definition -> translateMethod(definition, output));
        program.getDataDefinitions().forEach(dataType -> buildDestructor(dataType, output));
        translateFunction(program.getMain(), output);

        return output.getLines();
    }

    private static Set<String> collectStrings(BcProgram program) {
        StringsCollector collector = new StringsCollector();
        program.getMethods().stream()
            .flatMap(method -> method.getBody().stream())
            .forEach(line -> line.accept(collector));
        program.getFunctions().stream()
            .flatMap(function -> function.getBody().stream())
            .forEach(line -> line.accept(collector));
        program.getMain().getBody().forEach(line -> line.accept(collector));
        return collector.getStrings();
    }

    private void translateFunction(BcFunctionDefinition definition, AsmOutput output) {
        FunctionTranslator translator = new FunctionTranslator(env, output, gc);
        translator.translate(env.getFunctionSymbol(definition.getFunction()), null, definition.getParameters(),
            definition.getLocalVariables(), definition.getBody());
    }

    private void translateMethod(BcMethodDefinition definition, AsmOutput output) {
        FunctionTranslator translator = new FunctionTranslator(env, output, gc);
        translator.translate(env.getMethodSymbol(definition.getDataType(), definition.getMethod()),
            definition.getThisValue(), definition.getParameters(), definition.getLocalVariables(),
            definition.getBody());
    }

    private void buildDestructor(AsgDataType dataType, AsmOutput output) {
        FunctionTranslator translator = new FunctionTranslator(env, output, gc);
        translator.buildDestructor(dataType);
    }
}
