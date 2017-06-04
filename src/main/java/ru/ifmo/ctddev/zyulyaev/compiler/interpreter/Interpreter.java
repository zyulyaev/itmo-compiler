package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class Interpreter {
    public void interpret(AsgProgram program, InterpreterRuntime runtime) {
        FunctionTable table = new FunctionTable(program.getFunctionDefinitions(),
            program.getExternalFunctions(), runtime.getFunctionDefinitions());
        InterpreterContext rootContext = new InterpreterContext(table, null);
        StatementInterpreter interpreter = rootContext.asStatementInterpreter();
        program.getMain().accept(interpreter);
    }
}
