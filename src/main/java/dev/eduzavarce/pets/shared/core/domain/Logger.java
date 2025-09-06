package dev.eduzavarce.pets.shared.core.domain;

public interface Logger {
    void info(String message);

    void info(String message, Record context);

    void warning(String message);

    void warning(String message, Record context);

    void critical(String message);

    void critical(String message, Record context);
}
