package server.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public final class Logger {
    private final java.util.logging.Logger julLogger;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public enum Severity {
        DEBUG,
        INFO,
        ERROR
    }

    public Logger(String className, String logFilePattern) {
        this.julLogger = java.util.logging.Logger.getLogger(className);
        this.julLogger.setUseParentHandlers(false);
        Handler fileHandler;
        try {
            fileHandler = new FileHandler(logFilePattern);
            fileHandler.setFormatter(new MyFormatter());
        } catch (IOException | SecurityException e) {
            throw new RuntimeException("Unexpected error opening logging file. " + e);
        }
        this.julLogger.addHandler(fileHandler);
        this.julLogger.setLevel(Level.ALL);
    }

    public void console(String message) {
        console(message, Severity.INFO);
    }

    public void console(String message, Severity severity) {
        System.out.println(message);
        switch (severity) {
            case DEBUG -> debug(message);
            case INFO -> info(message);
            case ERROR -> error(message);
        }
    }

    public void debug(String message) {
        julLogger.fine(message);
    }

    public void info(String message) {
        julLogger.info(message);
    }

    public void error(String message) {
        julLogger.severe(message);
    }

    private class MyFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("%s %s [%s] - %s\n",
                    dateFormat.format(new Date(record.getMillis())),
                    record.getLevel(),
                    record.getLongThreadID(),
                    record.getMessage()
            );
        }
    }
}
