package dev.eduzavarce.pets.shared.exceptions;

import java.util.List;

public record ValidationErrorResponse(List<ValidationError> message) {
}
