package io.piotrjastrzebski.gdxjam.nta;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.spi.ContextAwareBase;

public class LogsConfigurator extends ContextAwareBase implements Configurator {
    @Override public void configure (LoggerContext lc) {
        // this is equivalent to desktop logger, more or less
        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
        ca.setContext(lc);
        ca.setName("console");

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [%15.15t] .\\(%-40.40(%file:%line\\)) : %msg%n");
        encoder.setContext(lc);
        encoder.start();

        ca.setEncoder(encoder);
        ca.start();

        Logger rootLogger = lc.getLogger("ROOT");
        rootLogger.addAppender(ca);
    }
}
