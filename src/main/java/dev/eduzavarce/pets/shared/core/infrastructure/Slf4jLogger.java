package dev.eduzavarce.pets.shared.core.infrastructure;

import dev.eduzavarce.pets.shared.core.domain.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogger implements Logger {
    private final org.slf4j.Logger logger;

    public Slf4jLogger(Class<?> context) {
        this.logger = LoggerFactory.getLogger(context);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, Record context) {
        logger.info("{} {}", message, context);
    }

    @Override
    public void warning(String message) {
        logger.warn(message);
    }

    @Override
    public void warning(String message, Record context) {
        logger.warn("{} {}", message, context);
    }

    @Override
    public void critical(String message) {
        logger.error(message);
    }

    @Override
    public void critical(String message, Record context) {
        logger.error("{} {}", message, context);
    }
}
