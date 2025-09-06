package dev.eduzavarce.pets.shared.exceptions;

public record ErrorResponse(String message, String status) {

    public ErrorResponse(String message) {
        this(message, "error");
    }

    public ErrorResponse(String message, String status) {
        this.message = message;
        this.status = "error";
    }
}
