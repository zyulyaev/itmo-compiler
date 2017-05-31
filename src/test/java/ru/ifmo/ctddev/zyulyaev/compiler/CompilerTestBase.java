package ru.ifmo.ctddev.zyulyaev.compiler;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.Parameterized.Parameter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
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

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder(new File("target"));

    private void testInMode(CompilerRunner.Mode mode) throws Exception {
        CompilerRunner runner = new CompilerRunner(mode, "");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (Scanner in = new Scanner(Files.newBufferedReader(set.getInput()));
             PrintWriter out = new PrintWriter(buffer))
        {
            runner.run(new CompilerRunner.FileSet(set.getCode(),null,null), in, out);
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

        Path asm = tmp.newFile(set.getName() + ".s").toPath();
        Path output = tmp.newFile(set.getName()).toPath();
        Path log = tmp.newFile(set.getName() + ".log").toPath();

        Assert.assertEquals("Compile " + set.getCode(), 0,
            runner.run(new CompilerRunner.FileSet(set.getCode(), asm, output), null, null));
        Assert.assertEquals("Run " + set.getCode(), 0, new ProcessBuilder(output.toString())
            .redirectInput(set.getInput().toFile())
            .redirectOutput(log.toFile())
            .start().waitFor());

        byte[] ans = Files.readAllBytes(log);
        byte[] orig = Files.readAllBytes(set.getOrig());
        Assert.assertArrayEquals("Result " + set.getCode(), orig, ans);
    }
}
