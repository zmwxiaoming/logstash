package org.logstash.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.status.StatusLogger;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

class LogTestUtils {

    private static final Logger LOGGER = StatusLogger.getLogger();

    static String loadLogFileContent(String logfileName) throws IOException {
        Path path = FileSystems.getDefault()
                .getPath(System.getProperty("user.dir"), System.getProperty("ls.logs"), logfileName);

        assertTrue("Log [" + path.toString() + "] file MUST exists", Files.exists(path));
        try (Stream<String> logLines = Files.lines(path)) {
            return logLines.collect(Collectors.joining());
        }
    }

    static void reloadLogConfiguration() {
        LoggerContext context = LoggerContext.getContext(false);
        context.stop(1, TimeUnit.SECONDS); // this forces the Log4j config to be discarded
    }

    static void deleteLogFile(String logfileName) throws IOException {
        Path path = FileSystems.getDefault()
                .getPath(System.getProperty("user.dir"), System.getProperty("ls.logs"), logfileName);
        Files.deleteIfExists(path);
    }

    // probably to be removed after confirmation that WinOS build is back to green
    static void pollingCheckExistence(String logfileName, int sleep, TimeUnit timeUnit) {
        Path path = FileSystems.getDefault()
                .getPath(System.getProperty("user.dir"), System.getProperty("ls.logs"), logfileName);
        pollingCheckExistence(path, sleep, timeUnit);
    }

    static void pollingCheckExistence(Path path, int sleep, TimeUnit timeUnit) {
        final int maxRetries = 5;
        int retries = 0;
        do {
            if (Files.notExists(path)) {
                LOGGER.error("File [{}] doesn't exists", path);
                return;
            }

            try {
                Stream<String> logLines = Files.lines(path);
                String logContent = logLines.collect(Collectors.joining("\n"));
                logLines.close();
                LOGGER.error("LOG CONTENT >>>\n {}\nLOG CONTENT >>> ", logContent);
//                System.out.println("LOG CONTENT >>>");
//                System.out.printf(logContent);
//                System.out.println("LOG CONTENT >>>");
            } catch (IOException ioex) {
                fail("The file should exists and must be readable");
            }

            try {
                Thread.sleep(timeUnit.toMillis(sleep));
            } catch (InterruptedException e) {
                // follows up
                Thread.currentThread().interrupt();
                break;
            }

            retries++;
        } while (retries < maxRetries);

        assertTrue("Exhausted 5 retries to the file: " + path + " but still exists",retries < maxRetries);
    }
}
