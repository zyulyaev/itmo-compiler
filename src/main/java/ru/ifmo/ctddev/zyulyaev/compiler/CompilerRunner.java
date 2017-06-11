package ru.ifmo.ctddev.zyulyaev.compiler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.Data;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import ru.ifmo.ctddev.zyulyaev.GrammarLexer;
import ru.ifmo.ctddev.zyulyaev.GrammarParser;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.asg.build.AsgBuilder;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.AsmTranslator;
import ru.ifmo.ctddev.zyulyaev.compiler.asm.line.AsmLine;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.BcInterpreter;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.interpreter.BcRuntime;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.model.BcProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.bytecode.translate.BcProgramTranslator;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.Interpreter;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.InterpreterRuntime;

import java.io.BufferedWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class CompilerRunner {
    private final Mode mode;
    private final String runtime;

    public CompilerRunner(Mode mode, String runtime) {
        this.mode = mode;
        this.runtime = runtime;
    }

    public int run(FileSet fileSet, Scanner in, PrintStream out) throws Exception {
        GrammarParser parser = new GrammarParser(new CommonTokenStream(new GrammarLexer(CharStreams.fromPath(fileSet.getInput()))));
        AsgProgram program = AsgBuilder.build(parser, Runtime.getExternalFunctions());
        switch (mode) {
            case INTERPRETER:
                new Interpreter(program, new InterpreterRuntime(in, out).getFunctionDefinitions()).interpret();
                return 0;
            case STACK_MACHINE: {
                BcProgram bcProgram = BcProgramTranslator.translate(program);
//                BcPrinter.print(bcProgram, System.out);
                new BcInterpreter(bcProgram, new BcRuntime(in, out).getFunctionDefinitions()).interpret();
                return 0;
            }
            case COMPILER: {
                BcProgram bcProgram = BcProgramTranslator.translate(program);
//                BcPrinter.print(bcProgram, System.out);
                try (BufferedWriter writer = Files.newBufferedWriter(fileSet.getAsmOutput())) {
                    for (AsmLine line : new AsmTranslator(bcProgram).translate()) {
//                        System.out.println(line.print());
                        writer.write(line.print());
                        writer.newLine();
                    }
                }
                ProcessBuilder gcc = new ProcessBuilder("gcc", "-m32", "-g", "-o",
                    fileSet.getOutput().toString(), runtime + "/runtime.o", fileSet.getAsmOutput().toString())
                    .inheritIO();
                return gcc.start().waitFor();
            }
        }
        return 1;
    }

    public static void main(String[] argv) throws Exception {
        Args args = new Args();
        JCommander jCommander = JCommander.newBuilder()
            .addObject(args)
            .build();
        jCommander.parse(argv);

        CompilerRunner runner = null;
        String runtime = runtimeFromEnv();
        if ((args.interpret ? 1 : 0) + (args.stackMachine ? 1 : 0) + (args.compile ? 1 : 0) > 1) {
            System.out.println("Please select only one mode");
        } else if (args.interpret) {
            runner = new CompilerRunner(Mode.INTERPRETER, runtime);
        } else if (args.stackMachine) {
            runner = new CompilerRunner(Mode.STACK_MACHINE, runtime);
        } else if (args.compile) {
            runner = new CompilerRunner(Mode.COMPILER, runtime);
        }
        if (runner != null) {
            int result;
            try (Scanner in = new Scanner(System.in)) {
                result = runner.run(FileSet.fromInput(args.files.get(0)), in, System.out);
            }
            System.exit(result);
        } else {
            jCommander.usage();
        }
    }

    private static String runtimeFromEnv() {
        String runtime = System.getenv("RC_RUNTIME");
        return runtime == null ? "../../runtime" : runtime;
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

    public enum Mode {
        INTERPRETER, STACK_MACHINE, COMPILER
    }

    @Data
    public static class FileSet {
        private final Path input;
        private final Path asmOutput;
        private final Path output;

        private static FileSet fromInput(String input) {
            String base = input.replace(".expr", "");
            return new FileSet(
                Paths.get(input),
                Paths.get(base + ".s"),
                Paths.get(base)
            );
        }
    }
}