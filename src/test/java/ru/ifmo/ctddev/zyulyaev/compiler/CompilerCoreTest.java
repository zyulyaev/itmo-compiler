package ru.ifmo.ctddev.zyulyaev.compiler;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Paths;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
@RunWith(Parameterized.class)
public class CompilerCoreTest extends CompilerTestBase {
    @Parameters
    public static Object[][] parameters() {
        return scanTests(Paths.get("tests", "core"));
    }
}
