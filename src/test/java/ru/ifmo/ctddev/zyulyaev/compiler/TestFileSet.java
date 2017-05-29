package ru.ifmo.ctddev.zyulyaev.compiler;

import lombok.Data;

import java.nio.file.Path;

/**
 * @author zyulyaev
 * @since 29.05.2017
 */
@Data
public class TestFileSet {
    private final Path code;
    private final Path input;
    private final Path orig;

    public static TestFileSet create(Path base, String testPrefix) {
        return new TestFileSet(
            base.resolve(testPrefix + ".expr"),
            base.resolve(testPrefix + ".input"),
            base.resolve("orig").resolve(testPrefix + ".log")
        );
    }
}
