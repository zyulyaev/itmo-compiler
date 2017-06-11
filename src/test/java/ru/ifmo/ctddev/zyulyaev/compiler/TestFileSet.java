package ru.ifmo.ctddev.zyulyaev.compiler;

import lombok.Data;

import java.nio.file.Path;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
@Data
public class TestFileSet {
    private final String name;
    private final Path code;
    private final Path input;
    private final Path origLog;
    private final Path origErr;

    public static TestFileSet create(Path base, String testPrefix) {
        Path orig = base.resolve("orig");
        return new TestFileSet(
            testPrefix,
            base.resolve(testPrefix + ".expr"),
            base.resolve(testPrefix + ".input"),
            orig.resolve(testPrefix + ".log"),
            orig.resolve(testPrefix + ".err")
        );
    }
}
