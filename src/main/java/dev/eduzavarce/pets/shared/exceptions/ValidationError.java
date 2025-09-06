package dev.eduzavarce.pets.shared.exceptions;

public record ValidationError(String field, String message) {}
