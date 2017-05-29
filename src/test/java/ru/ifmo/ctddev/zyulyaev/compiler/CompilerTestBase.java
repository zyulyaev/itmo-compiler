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

    private void testInMode(CompilerRunner.Mode mode) throws IOException {
        System.err.println("TestSet: " + set);
        CompilerRunner runner = new CompilerRunner(mode);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream in = Files.newInputStream(set.getInput());
             PrintStream out = new PrintStream(buffer))
        {
            System.setIn(in);
            System.setOut(out);

            runner.run(set.getCode());
        }
        byte[] ans = buffer.toByteArray();
        byte[] orig = Files.readAllBytes(set.getOrig());
        Assert.assertArrayEquals(orig, ans);
    }

    @Test
    public void testInterpreter() throws IOException {
        testInMode(CompilerRunner.Mode.INTERPRETER);
    }

    @Test
    public void testStackMachine() throws IOException {
        testInMode(CompilerRunner.Mode.STACK_MACHINE);
    }
}
