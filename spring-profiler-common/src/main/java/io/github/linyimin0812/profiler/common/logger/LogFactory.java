package io.github.linyimin0812.profiler.common.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import io.github.linyimin0812.profiler.common.utils.AgentHomeUtil;
import org.slf4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author linyimin
 **/
public class LogFactory {

    private final static LoggerContext SINGLETON = new LoggerContext();

    static {
        createLogger(LoggerName.startup.name(), "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %class - %msg%n");
        createLogger(LoggerName.transform.name(), "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] - %msg%n");
    }

    public static Logger getStartupLogger() {
        return SINGLETON.getLogger(LoggerName.startup.name());
    }

    public static Logger getTransFormLogger() {
        return SINGLETON.getLogger(LoggerName.transform.name());
    }

    private static void createLogger(String name, String pattern) {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();

        appender.setContext(SINGLETON);
        appender.setName(name);
        appender.setAppend(true);
        appender.setFile(getLogPath() + name + ".log");

        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setContext(SINGLETON);
        triggeringPolicy.start();

        appender.setTriggeringPolicy(triggeringPolicy);

        FixedWindowRollingPolicy rolling = new FixedWindowRollingPolicy();
        rolling.setContext(SINGLETON);
        rolling.setFileNamePattern(getLogPath() + name + ".%i.log");
        rolling.setParent(appender);
        rolling.setMaxIndex(5);
        rolling.start();

        appender.setRollingPolicy(rolling);

        PatternLayoutEncoder layout = setPatternLayout(pattern);

        appender.setEncoder(layout);

        appender.start();

        ch.qos.logback.classic.Logger logger = SINGLETON.getLogger(name);
        logger.detachAndStopAllAppenders();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

        addConsoleAppender(logger, pattern);

    }

    private static void addConsoleAppender(ch.qos.logback.classic.Logger logger, String pattern) {

        // 为了方便调试，通过环境变量配置日志输出到console
        String profile = System.getProperty("spring-startup-analyzer.boost.profile");
        if (!"dev".equals(profile)) {
            return;
        }

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(new LoggerContext());
        consoleAppender.setLayout(setPatternLayout(pattern).getLayout());
        consoleAppender.start();
        logger.addAppender(consoleAppender);
    }

    private static PatternLayoutEncoder setPatternLayout(String pattern) {
        PatternLayoutEncoder layout = new PatternLayoutEncoder();
        layout.setPattern(pattern);
        layout.setContext(SINGLETON);
        layout.setCharset(StandardCharsets.UTF_8);
        layout.start();

        return layout;
    }

    private static String getLogPath() {
        // ~/spring-startup-analyzer/logs/
        return AgentHomeUtil.home() + "logs" + File.separator;
    }
}
