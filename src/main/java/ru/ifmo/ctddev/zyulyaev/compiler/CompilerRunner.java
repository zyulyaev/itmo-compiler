package ru.ifmo.ctddev.zyulyaev.compiler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import ru.ifmo.ctddev.zyulyaev.GrammarLexer;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.build.AsgBuilder;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.BcInterpreter;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.BcRuntime;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate.BcTranslator;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.Interpreter;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.InterpreterRuntime;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class CompilerRunner {
    private final Mode mode;

    public CompilerRunner(Mode mode) {
        this.mode = mode;
    }

    public void run(Path path) throws IOException {
        GrammarParser parser = new GrammarParser(new CommonTokenStream(new GrammarLexer(CharStreams.fromPath(path))));
        AsgProgram program = AsgBuilder.build(parser, Runtime.getExternalFunctions());
        switch (mode) {
        case INTERPRETER:
            new Interpreter().interpret(program, new InterpreterRuntime().getFunctionDefinitions());
            break;
        case STACK_MACHINE:
            BcProgram bcProgram = new BcTranslator().translate(program);
//            try (BcPrinter bcPrinter = new BcPrinter(new PrintWriter(System.err))) {
//                bcPrinter.print(bcProgram);
//            }
            new BcInterpreter(bcProgram, new BcRuntime().getFunctionDefinitions()).interpret();
            break;
        case COMPILER:
            throw new UnsupportedOperationException("Compiler mode is not supported");
        }
    }

    public static void main(String[] argv) throws IOException {
        Args args = new Args();
        JCommander jCommander = JCommander.newBuilder()
            .addObject(args)
            .build();
        jCommander.parse(argv);

        CompilerRunner runner = null;
        if ((args.interpret ? 1 : 0) + (args.stackMachine ? 1 : 0) + (args.compile ? 1 : 0) > 1) {
            System.out.println("Please select only one mode");
        } else if (args.interpret) {
            runner = new CompilerRunner(Mode.INTERPRETER);
        } else if (args.stackMachine) {
            runner = new CompilerRunner(Mode.STACK_MACHINE);
        } else if (args.compile) {
            runner = new CompilerRunner(Mode.COMPILER);
        }
        if (runner != null) {
            runner.run(Paths.get(args.files.get(0)));
        } else {
            jCommander.usage();
        }
    }

    private static class Args {
        @Parameter(description = "<File to interpret/compile>")
        private List<String> files;

        @Parameter(names = "-i", description = "Interpretation mode")
        private boolean interpret;

        @Parameter(names = "-s", description = "Stack machine interpretation mode")
        private boolean stackMachine;

        @Parameter(names = "-o", description = "Compilation mode")
        private boolean compile;
    }

    private enum Mode {
        INTERPRETER, STACK_MACHINE, COMPILER
    }
}