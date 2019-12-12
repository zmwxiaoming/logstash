package org.logstash.log;

import org.apache.logging.log4j.core.LoggerContext;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

class LogTestUtils {

    static String loadLogFileContent(String logfileName) throws IOException {
        Path path = FileSystems.getDefault()
                .getPath(System.getProperty("user.dir"), System.getProperty("ls.logs"), logfileName);

        assertTrue("Log [" + path.toString() + "] file MUST exists", Files.exists(path));
        return Files.lines(path).collect(Collectors.joining());
    }

    static void reloadLogConfiguration() {
        LoggerContext context = LoggerContext.getContext(false);
        context.stop(1, TimeUnit.SECONDS); // this forces the Log4j config to be discarded
    }

    static void deleteLogFile(String logfileName) throws IOException {
        Path path = FileSystems.getDefault()
                .getPath(System.getProperty("user.dir"), System.getProperty("ls.logs"), logfileName);
        Files.deleteIfExists(path);
//        if (Files.exists(path)) {
//            System.out.println("File: " + path + " exists");
//            Files.delete(path);
//            if (Files.exists(path)) {
//                System.out.println("Weird the file exists after deletion");
//            }
//        } else {
//            System.out.println("File: " + path + " doesn't exists");
//        }
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
                System.out.println("File [" + path + "] doesn't exists");
                return;
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
