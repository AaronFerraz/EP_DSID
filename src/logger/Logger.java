package logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final String className;

    public Logger(Class<?> clazz) {
        this.className = clazz.getSimpleName();
    }

    public void logDebug(String message) {
        System.out.printf("[%s] [%s] [%s] %s%n", dateTimeFormatter.format(LocalDateTime.now()), Thread.currentThread().getName(), className, message);
    }

    public void log(String message, Boolean breakLine) {
        if (breakLine)
            System.out.printf("%s%n", message);
        else
            System.out.printf("%s", message);
    }

    public void log(String message, Object... args) {
        this.log(String.format(message, args), true);
    }
}
