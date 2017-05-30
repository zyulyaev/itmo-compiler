package ru.ifmo.ctddev.zyulyaev.compiler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.StreamSupport;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
public abstract class CompilerTestBase {
    protected static Object[][] scanTests(Path base) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(base, "*.expr")) {
            return StreamSupport.stream(stream.spliterator(), false)
                .map(path -> TestFileSet.create(base, path.getFileName().toString().replace(".expr", "")))
                .map(set -> new Object[]{set})
                .toArray(Object[][]::new);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Parameter
    public TestFileSet set;

    private void testInMode(CompilerRunner.Mode mode) throws Exception {
        CompilerRunner runner = new CompilerRunner(mode, "");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream in = Files.newInputStream(set.getInput());
             PrintStream out = new PrintStream(buffer))
        {
            System.setIn(in);
            System.setOut(out);

            runner.run(new CompilerRunner.FileSet(
                set.getCode(),
                null,
                null
            ));
        }
        byte[] ans = buffer.toByteArray();
        byte[] orig = Files.readAllBytes(set.getOrig());
        Assert.assertArrayEquals(orig, ans);
    }

    @Test
    public void testInterpreter() throws Exception {
        testInMode(CompilerRunner.Mode.INTERPRETER);
    }

    @Test
    public void testStackMachine() throws Exception {
        testInMode(CompilerRunner.Mode.STACK_MACHINE);
    }

    @Test
    public void testCompiler() throws Exception {
        CompilerRunner runner = new CompilerRunner(CompilerRunner.Mode.COMPILER, "runtime");
        Path asm = Paths.get("target","test.s");
        Path output = Paths.get("target","test");
        Path log = Paths.get("target", "test.log");
        try {
            Assert.assertEquals("Compile " + set.getCode(), 0,
                runner.run(new CompilerRunner.FileSet(set.getCode(), asm, output)));
            Assert.assertEquals("Run " + set.getCode(), 0, new ProcessBuilder(output.toString())
                .redirectInput(set.getInput().toFile())
                .redirectOutput(log.toFile())
                .start().waitFor());

            byte[] ans = Files.readAllBytes(log);
            byte[] orig = Files.readAllBytes(set.getOrig());
            Assert.assertArrayEquals("Result " + set.getCode(), orig, ans);
        } finally {
            Files.deleteIfExists(asm);
            Files.deleteIfExists(output);
            Files.deleteIfExists(log);
        }
    }
}
