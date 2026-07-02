package com.manzhushaka.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * 日志规范守护。
 *
 * @author manzhushaka
 */
class LoggingConventionTest
{
    @Test
    void productionCodeShouldNotWriteDirectlyToConsoleOrPrintStackTrace() throws IOException
    {
        List<String> violations = new ArrayList<>();
        Path projectRoot = findProjectRoot();

        try (Stream<Path> files = Files.walk(projectRoot))
        {
            files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("/src/main/java/"))
                    .filter(path -> !path.toString().contains("/target/"))
                    .filter(path -> !path.toString().contains("/.worktrees/"))
                    .forEach(path -> collectViolations(projectRoot, path, violations));
        }

        assertThat(violations)
                .as("生产源码禁止直接使用 System.out/System.err 或裸 e.printStackTrace()")
                .isEmpty();
    }

    private static void collectViolations(Path projectRoot, Path path, List<String> violations)
    {
        try
        {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++)
            {
                String line = lines.get(i);
                if (line.contains("System.out.") || line.contains("System.err.") || line.contains(".printStackTrace();"))
                {
                    violations.add(projectRoot.relativize(path) + ":" + (i + 1) + " " + line.trim());
                }
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to inspect " + path, e);
        }
    }

    private static Path findProjectRoot()
    {
        Path current = Path.of("").toAbsolutePath();
        while (current != null)
        {
            if (Files.exists(current.resolve("manzhushaka-admin"))
                    && Files.exists(current.resolve("manzhushaka-framework"))
                    && Files.exists(current.resolve("AGENTS.md")))
            {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate project root");
    }
}
