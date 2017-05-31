package ru.ifmo.ctddev.zyulyaev.compiler.interpreter;

import ru.ifmo.ctddev.zyulyaev.compiler.asg.AsgProgram;
import ru.ifmo.ctddev.zyulyaev.compiler.interpreter.value.Value;
import ru.ifmo.ctddev.zyulyaev.compiler.lang.ExternalFunction;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zyulyaev
 * @since 27.05.2017
 */
public class Interpreter {
    public void interpret(AsgProgram program,
        Map<ExternalFunction, Function<List<Value>, Value>> externalFunctionDefinitionMap)
    {
        FunctionTable table = new FunctionTable(program.getFunctionDefinitions(),
            program.getExternalDefinitions().inverse(), externalFunctionDefinitionMap);
        InterpreterContext rootContext = new InterpreterContext(table, null);
        StatementInterpreter interpreter = rootContext.asStatementInterpreter();
        program.getMain().accept(interpreter);
    }
}
